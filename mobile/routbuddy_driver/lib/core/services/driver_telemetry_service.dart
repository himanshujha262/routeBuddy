import 'dart:async';
import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:geolocator/geolocator.dart';
import 'package:stomp_dart_client/stomp.dart';
import 'package:stomp_dart_client/stomp_config.dart';
import 'package:stomp_dart_client/stomp_frame.dart';
import '../constants/api_constants.dart';
import '../network/api_client.dart';
import '../storage/token_storage.dart';

class DriverTelemetryState {
  final bool isBroadcasting;
  final bool isConnected;
  final bool isReconnecting;
  final int pingCount;
  final double currentSpeedKmph;
  final double heading;
  final double latitude;
  final double longitude;
  final double accuracyMeters;
  final DateTime? lastPingAt;
  final String? lastError;

  const DriverTelemetryState({
    this.isBroadcasting = false,
    this.isConnected = false,
    this.isReconnecting = false,
    this.pingCount = 0,
    this.currentSpeedKmph = 0.0,
    this.heading = 0.0,
    this.latitude = 0.0,
    this.longitude = 0.0,
    this.accuracyMeters = 0.0,
    this.lastPingAt,
    this.lastError,
  });

  DriverTelemetryState copyWith({
    bool? isBroadcasting,
    bool? isConnected,
    bool? isReconnecting,
    int? pingCount,
    double? currentSpeedKmph,
    double? heading,
    double? latitude,
    double? longitude,
    double? accuracyMeters,
    DateTime? lastPingAt,
    String? lastError,
  }) {
    return DriverTelemetryState(
      isBroadcasting: isBroadcasting ?? this.isBroadcasting,
      isConnected: isConnected ?? this.isConnected,
      isReconnecting: isReconnecting ?? this.isReconnecting,
      pingCount: pingCount ?? this.pingCount,
      currentSpeedKmph: currentSpeedKmph ?? this.currentSpeedKmph,
      heading: heading ?? this.heading,
      latitude: latitude ?? this.latitude,
      longitude: longitude ?? this.longitude,
      accuracyMeters: accuracyMeters ?? this.accuracyMeters,
      lastPingAt: lastPingAt ?? this.lastPingAt,
      lastError: lastError,
    );
  }
}

class DriverTelemetryService extends ChangeNotifier {
  static final DriverTelemetryService _instance = DriverTelemetryService._internal();
  factory DriverTelemetryService() => _instance;
  DriverTelemetryService._internal();

  DriverTelemetryState _state = const DriverTelemetryState();
  DriverTelemetryState get state => _state;

  StompClient? _stompClient;
  StreamSubscription<Position>? _positionSubscription;
  String? _activeTripId;
  String? _activeDriverId;
  Timer? _fallbackTimer;

  Future<bool> startTracking({
    required String tripId,
    required String driverId,
  }) async {
    if (_state.isBroadcasting && _activeTripId == tripId) return true;

    _activeTripId = tripId;
    _activeDriverId = driverId;

    // 1. Request GPS permissions
    LocationPermission permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
      if (permission == LocationPermission.denied) {
        _updateState(_state.copyWith(lastError: 'Location permission denied'));
        return false;
      }
    }

    if (permission == LocationPermission.deniedForever) {
      _updateState(_state.copyWith(lastError: 'Location permissions permanently denied'));
      return false;
    }

    bool serviceEnabled = await Geolocator.isLocationServiceEnabled();
    if (!serviceEnabled) {
      _updateState(_state.copyWith(lastError: 'GPS location services are disabled on device'));
      return false;
    }

    _updateState(_state.copyWith(
      isBroadcasting: true,
      isReconnecting: true,
      pingCount: 0,
      lastError: null,
    ));

    // 2. Connect WebSocket STOMP client
    await _connectWebSocket();

    // 3. Listen to live GPS position stream
    const locationSettings = LocationSettings(
      accuracy: LocationAccuracy.high,
      distanceFilter: 5,
    );

    _positionSubscription = Geolocator.getPositionStream(
      locationSettings: locationSettings,
    ).listen(
      _handlePositionUpdate,
      onError: (error) {
        _updateState(_state.copyWith(lastError: 'GPS stream error: $error'));
      },
    );

    // Fallback periodic timer in case vehicle is stationary or stream lags
    _fallbackTimer = Timer.periodic(const Duration(seconds: 4), (_) async {
      if (_state.isBroadcasting) {
        try {
          final pos = await Geolocator.getCurrentPosition(
            desiredAccuracy: LocationAccuracy.high,
          );
          _handlePositionUpdate(pos);
        } catch (_) {}
      }
    });

    return true;
  }

  Future<void> _connectWebSocket() async {
    final token = await TokenStorage.getAccessToken();
    final wsBase = ApiConstants.baseUrl.replaceFirst(RegExp(r'^http'), 'ws');
    final wsUrl = '$wsBase/ws/telemetry-raw';

    _stompClient?.deactivate();

    _stompClient = StompClient(
      config: StompConfig(
        url: wsUrl,
        onConnect: (StompFrame frame) {
          _updateState(_state.copyWith(
            isConnected: true,
            isReconnecting: false,
            lastError: null,
          ));
        },
        onWebSocketError: (dynamic error) {
          _updateState(_state.copyWith(
            isConnected: false,
            isReconnecting: true,
            lastError: 'WebSocket connection error. Reconnecting...',
          ));
        },
        onDisconnect: (StompFrame frame) {
          _updateState(_state.copyWith(
            isConnected: false,
            isReconnecting: _state.isBroadcasting,
          ));
        },
        stompConnectHeaders: {
          if (token != null) 'Authorization': 'Bearer $token',
        },
        webSocketConnectHeaders: {
          if (token != null) 'Authorization': 'Bearer $token',
        },
        reconnectDelay: const Duration(seconds: 3),
        heartbeatIncoming: const Duration(seconds: 10),
        heartbeatOutgoing: const Duration(seconds: 10),
      ),
    );

    _stompClient?.activate();
  }

  Future<void> _handlePositionUpdate(Position pos) async {
    if (!_state.isBroadcasting || _activeTripId == null) return;

    final speedKmph = (pos.speed * 3.6).clamp(0.0, 120.0);
    final heading = pos.heading.isFinite ? pos.heading : 0.0;

    final pingPayload = {
      'driverId': _activeDriverId,
      'tripId': _activeTripId,
      'latitude': pos.latitude,
      'longitude': pos.longitude,
      'heading': heading,
      'speedKmph': speedKmph,
      'accuracyMeters': pos.accuracy,
      'timestamp': DateTime.now().toUtc().toIso8601String(),
    };

    bool sentOverWs = false;

    // Try WebSocket transmission
    if (_stompClient != null && _stompClient!.connected) {
      try {
        _stompClient!.send(
          destination: '/app/trips/$_activeTripId/location',
          body: jsonEncode(pingPayload),
        );
        sentOverWs = true;
      } catch (_) {
        sentOverWs = false;
      }
    }

    // Fallback over REST API if WebSocket is offline or disconnected
    if (!sentOverWs) {
      try {
        await ApiClient().dio.post(
          '${ApiConstants.baseUrl}/api/v1/locations/trips/$_activeTripId/telemetry',
          data: pingPayload,
        );
      } catch (_) {}
    }

    _updateState(_state.copyWith(
      pingCount: _state.pingCount + 1,
      currentSpeedKmph: speedKmph,
      heading: heading,
      latitude: pos.latitude,
      longitude: pos.longitude,
      accuracyMeters: pos.accuracy,
      lastPingAt: DateTime.now(),
    ));
  }

  void stopTracking() {
    _positionSubscription?.cancel();
    _positionSubscription = null;
    _fallbackTimer?.cancel();
    _fallbackTimer = null;

    _stompClient?.deactivate();
    _stompClient = null;

    _activeTripId = null;
    _activeDriverId = null;

    _updateState(const DriverTelemetryState(isBroadcasting: false));
  }

  void _updateState(DriverTelemetryState newState) {
    _state = newState;
    notifyListeners();
  }
}
