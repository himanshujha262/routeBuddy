import 'dart:async';
import 'dart:convert';
import 'dart:math' as math;
import 'package:flutter/material.dart';
import 'package:stomp_dart_client/stomp.dart';
import 'package:stomp_dart_client/stomp_config.dart';
import 'package:stomp_dart_client/stomp_frame.dart';
import '../../../core/constants/api_constants.dart';
import '../../../core/network/api_client.dart';
import '../../../core/storage/token_storage.dart';

class LiveTripTrackingScreen extends StatefulWidget {
  final dynamic trip;
  final dynamic route;

  const LiveTripTrackingScreen({
    Key? key,
    required this.trip,
    this.route,
  }) : super(key: key);

  @override
  State<LiveTripTrackingScreen> createState() => _LiveTripTrackingScreenState();
}

class _LiveTripTrackingScreenState extends State<LiveTripTrackingScreen> {
  StompClient? _stompClient;
  bool _isConnected = false;
  bool _isReconnecting = false;
  Timer? _fallbackPollTimer;

  // Live Telemetry State
  double _latitude = 28.5355;
  double _longitude = 77.3910;
  double _heading = 0.0;
  double _speedKmph = 0.0;
  int? _currentStopSequence;
  DateTime? _lastPingTime;

  // Live Occupancy State
  int _totalSeats = 3;
  int _availableSeats = 1;
  int _bookedSeats = 2;
  double _occupancyPercent = 66.7;
  String _tripStatus = 'IN_TRANSIT';

  bool _isLoading = true;
  String? _errorMessage;

  String get _tripId => (widget.trip['tripId'] ?? widget.trip['id']).toString();

  @override
  void initState() {
    super.initState();
    _totalSeats = (widget.trip['totalSeats'] as num?)?.toInt() ?? 3;
    _availableSeats = (widget.trip['availableSeats'] as num?)?.toInt() ?? 1;
    _bookedSeats = _totalSeats - _availableSeats;
    _occupancyPercent = _totalSeats > 0 ? (_bookedSeats * 100.0) / _totalSeats : 0.0;
    _tripStatus = widget.trip['status'] ?? 'IN_TRANSIT';

    _fetchInitialLiveState();
    _connectWebSocket();
    _startFallbackPolling();
  }

  @override
  void dispose() {
    _fallbackPollTimer?.cancel();
    _stompClient?.deactivate();
    super.dispose();
  }

  Future<void> _fetchInitialLiveState() async {
    try {
      final response = await ApiClient().dio.get(
        '${ApiConstants.baseUrl}/api/v1/locations/trips/$_tripId',
      );

      if (response.statusCode == 200 && response.data['data'] != null) {
        final data = response.data['data'];
        final telemetry = data['telemetry'];
        final occupancy = data['occupancy'];

        if (telemetry != null) {
          setState(() {
            _latitude = (telemetry['latitude'] as num?)?.toDouble() ?? _latitude;
            _longitude = (telemetry['longitude'] as num?)?.toDouble() ?? _longitude;
            _heading = (telemetry['heading'] as num?)?.toDouble() ?? _heading;
            _speedKmph = (telemetry['speedKmph'] as num?)?.toDouble() ?? _speedKmph;
            _currentStopSequence = (telemetry['currentStopSequence'] as num?)?.toInt();
            if (telemetry['timestamp'] != null) {
              _lastPingTime = DateTime.tryParse(telemetry['timestamp']);
            }
          });
        }

        if (occupancy != null) {
          setState(() {
            _totalSeats = (occupancy['totalSeats'] as num?)?.toInt() ?? _totalSeats;
            _availableSeats = (occupancy['availableSeats'] as num?)?.toInt() ?? _availableSeats;
            _bookedSeats = (occupancy['bookedSeats'] as num?)?.toInt() ?? _bookedSeats;
            _occupancyPercent = (occupancy['occupancyPercentage'] as num?)?.toDouble() ?? _occupancyPercent;
            _tripStatus = occupancy['status'] ?? _tripStatus;
          });
        }
      }
    } catch (_) {
      // Fallback handled gracefully
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
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
          if (!mounted) return;
          setState(() {
            _isConnected = true;
            _isReconnecting = false;
          });

          // Subscribe to Trip Location Topic
          _stompClient?.subscribe(
            destination: '/topic/trips/$_tripId/location',
            callback: (StompFrame frame) {
              if (frame.body != null) {
                try {
                  final data = jsonDecode(frame.body!);
                  if (mounted) {
                    setState(() {
                      _latitude = (data['latitude'] as num?)?.toDouble() ?? _latitude;
                      _longitude = (data['longitude'] as num?)?.toDouble() ?? _longitude;
                      _heading = (data['heading'] as num?)?.toDouble() ?? _heading;
                      _speedKmph = (data['speedKmph'] as num?)?.toDouble() ?? _speedKmph;
                      _currentStopSequence = (data['currentStopSequence'] as num?)?.toInt() ?? _currentStopSequence;
                      _lastPingTime = DateTime.now();
                    });
                  }
                } catch (_) {}
              }
            },
          );

          // Subscribe to Trip Occupancy Topic
          _stompClient?.subscribe(
            destination: '/topic/trips/$_tripId/occupancy',
            callback: (StompFrame frame) {
              if (frame.body != null) {
                try {
                  final data = jsonDecode(frame.body!);
                  if (mounted) {
                    setState(() {
                      _totalSeats = (data['totalSeats'] as num?)?.toInt() ?? _totalSeats;
                      _availableSeats = (data['availableSeats'] as num?)?.toInt() ?? _availableSeats;
                      _bookedSeats = (data['bookedSeats'] as num?)?.toInt() ?? _bookedSeats;
                      _occupancyPercent = (data['occupancyPercentage'] as num?)?.toDouble() ?? _occupancyPercent;
                      _tripStatus = data['status'] ?? _tripStatus;
                    });
                  }
                } catch (_) {}
              }
            },
          );
        },
        onWebSocketError: (dynamic error) {
          if (mounted) {
            setState(() {
              _isConnected = false;
              _isReconnecting = true;
            });
          }
        },
        onDisconnect: (StompFrame frame) {
          if (mounted) {
            setState(() {
              _isConnected = false;
              _isReconnecting = true;
            });
          }
        },
        stompConnectHeaders: {
          if (token != null) 'Authorization': 'Bearer $token',
        },
        webSocketConnectHeaders: {
          if (token != null) 'Authorization': 'Bearer $token',
        },
        reconnectDelay: const Duration(seconds: 4),
        heartbeatIncoming: const Duration(seconds: 10),
        heartbeatOutgoing: const Duration(seconds: 10),
      ),
    );

    _stompClient?.activate();
  }

  void _startFallbackPolling() {
    _fallbackPollTimer = Timer.periodic(const Duration(seconds: 6), (_) {
      if (!_isConnected) {
        _fetchInitialLiveState();
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final vehiclePlate = widget.trip['vehiclePlateNumber'] ?? 'Shared Auto';
    final routeName = widget.route?['name'] ?? widget.trip['routeName'] ?? 'Transit Corridor';
    final List<dynamic> stops = (widget.route?['stops'] as List<dynamic>?) ?? [];

    return Scaffold(
      backgroundColor: const Color(0xFF0B0F19),
      appBar: AppBar(
        backgroundColor: const Color(0xFF111827),
        elevation: 0,
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Live Trip Tracking',
              style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: Colors.white),
            ),
            Text(
              routeName,
              style: const TextStyle(fontSize: 11, color: Colors.grey),
            ),
          ],
        ),
        actions: [
          Container(
            margin: const EdgeInsets.symmetric(vertical: 12, horizontal: 12),
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
            decoration: BoxDecoration(
              color: _isConnected
                  ? const Color(0xFF10B981).withOpacity(0.15)
                  : Colors.amber.withOpacity(0.15),
              borderRadius: BorderRadius.circular(6),
              border: Border.all(
                color: _isConnected ? const Color(0xFF10B981) : Colors.amber,
              ),
            ),
            child: Row(
              children: [
                Container(
                  width: 6,
                  height: 6,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: _isConnected ? const Color(0xFF10B981) : Colors.amber,
                  ),
                ),
                const SizedBox(width: 6),
                Text(
                  _isConnected ? 'Live WebSocket' : (_isReconnecting ? 'Reconnecting...' : 'Polling Fallback'),
                  style: TextStyle(
                    color: _isConnected ? const Color(0xFF10B981) : Colors.amber,
                    fontSize: 10,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Live Interactive Map Viewport Simulation Canvas
            Container(
              height: 240,
              decoration: BoxDecoration(
                color: const Color(0xFF131B2A),
                borderRadius: BorderRadius.circular(20),
                border: Border.all(color: const Color(0xFF1F2937)),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withOpacity(0.4),
                    blurRadius: 12,
                    offset: const Offset(0, 4),
                  ),
                ],
              ),
              child: Stack(
                children: [
                  // Map Grid Lines
                  CustomPaint(
                    size: const Size(double.infinity, 240),
                    painter: _MapGridPainter(),
                  ),

                  // Route Corridor Polyline Overlay
                  Center(
                    child: Container(
                      width: 220,
                      height: 4,
                      decoration: BoxDecoration(
                        gradient: const LinearGradient(
                          colors: [Color(0xFF10B981), Color(0xFF3B82F6), Color(0xFF8B5CF6)],
                        ),
                        borderRadius: BorderRadius.circular(2),
                      ),
                    ),
                  ),

                  // Live Auto Rickshaw Marker
                  Center(
                    child: Transform.rotate(
                      angle: (_heading * math.pi) / 180,
                      child: Container(
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          color: const Color(0xFF10B981),
                          boxShadow: [
                            BoxShadow(
                              color: const Color(0xFF10B981).withOpacity(0.5),
                              blurRadius: 16,
                              spreadRadius: 4,
                            ),
                          ],
                        ),
                        child: const Icon(
                          Icons.navigation,
                          color: Colors.white,
                          size: 28,
                        ),
                      ),
                    ),
                  ),

                  // Telemetry HUD Pill (Top Left)
                  Positioned(
                    top: 12,
                    left: 12,
                    child: Container(
                      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                      decoration: BoxDecoration(
                        color: const Color(0xFF0B0F19).withOpacity(0.85),
                        borderRadius: BorderRadius.circular(8),
                        border: Border.all(color: const Color(0xFF374151)),
                      ),
                      child: Row(
                        children: [
                          const Icon(Icons.speed, color: Color(0xFF10B981), size: 14),
                          const SizedBox(width: 6),
                          Text(
                            '${_speedKmph.toStringAsFixed(1)} km/h',
                            style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 12),
                          ),
                          const SizedBox(width: 8),
                          Text(
                            'Bearing: ${_heading.toStringAsFixed(0)}°',
                            style: const TextStyle(color: Colors.grey, fontSize: 11),
                          ),
                        ],
                      ),
                    ),
                  ),

                  // Vehicle Badge (Bottom Left)
                  Positioned(
                    bottom: 12,
                    left: 12,
                    child: Container(
                      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                      decoration: BoxDecoration(
                        color: const Color(0xFF0B0F19).withOpacity(0.85),
                        borderRadius: BorderRadius.circular(8),
                        border: Border.all(color: const Color(0xFF374151)),
                      ),
                      child: Row(
                        children: [
                          const Icon(Icons.electric_rickshaw, color: Color(0xFF10B981), size: 16),
                          const SizedBox(width: 6),
                          Text(
                            vehiclePlate,
                            style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 12),
                          ),
                        ],
                      ),
                    ),
                  ),

                  // GPS Ping Indicator (Bottom Right)
                  Positioned(
                    bottom: 12,
                    right: 12,
                    child: Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                      decoration: BoxDecoration(
                        color: const Color(0xFF0B0F19).withOpacity(0.85),
                        borderRadius: BorderRadius.circular(6),
                      ),
                      child: Text(
                        _lastPingTime != null
                            ? 'GPS: ${_lastPingTime!.hour.toString().padLeft(2, '0')}:${_lastPingTime!.minute.toString().padLeft(2, '0')}:${_lastPingTime!.second.toString().padLeft(2, '0')}'
                            : 'GPS Ready',
                        style: const TextStyle(color: Colors.grey, fontSize: 10),
                      ),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),

            // Real-Time Occupancy & Capacity Meter Card
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: const Color(0xFF182234),
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: const Color(0xFF1F2937)),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text(
                        'REAL-TIME OCCUPANCY',
                        style: TextStyle(
                          color: Color(0xFF10B981),
                          fontSize: 11,
                          fontWeight: FontWeight.bold,
                          letterSpacing: 1.1,
                        ),
                      ),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                        decoration: BoxDecoration(
                          color: const Color(0xFF10B981).withOpacity(0.15),
                          borderRadius: BorderRadius.circular(4),
                        ),
                        child: Text(
                          '$_availableSeats Seats Left',
                          style: const TextStyle(
                            color: Color(0xFF10B981),
                            fontSize: 11,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              '$_bookedSeats of $_totalSeats Seats Booked',
                              style: const TextStyle(
                                color: Colors.white,
                                fontWeight: FontWeight.bold,
                                fontSize: 15,
                              ),
                            ),
                            const SizedBox(height: 4),
                            Text(
                              '${_occupancyPercent.toStringAsFixed(0)}% Vehicle Capacity Utilized',
                              style: const TextStyle(color: Colors.grey, fontSize: 12),
                            ),
                          ],
                        ),
                      ),
                      Container(
                        padding: const EdgeInsets.all(10),
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          color: const Color(0xFF10B981).withOpacity(0.15),
                        ),
                        child: const Icon(Icons.people, color: Color(0xFF10B981), size: 24),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  // Progress Bar
                  ClipRRect(
                    borderRadius: BorderRadius.circular(4),
                    child: LinearProgressIndicator(
                      value: _totalSeats > 0 ? (_bookedSeats / _totalSeats).clamp(0.0, 1.0) : 0.0,
                      backgroundColor: const Color(0xFF1F2937),
                      valueColor: AlwaysStoppedAnimation<Color>(
                        _availableSeats == 0 ? Colors.redAccent : const Color(0xFF10B981),
                      ),
                      minHeight: 8,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),

            // Route Corridor Sequential Stops Timeline
            if (stops.isNotEmpty)
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: const Color(0xFF182234),
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: const Color(0xFF1F2937)),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'ROUTE STOPS & TIMELINE',
                      style: TextStyle(
                        color: Color(0xFF10B981),
                        fontSize: 11,
                        fontWeight: FontWeight.bold,
                        letterSpacing: 1.1,
                      ),
                    ),
                    const SizedBox(height: 12),
                    ...stops.map((stop) {
                      final seq = (stop['sequenceOrder'] as num?)?.toInt() ?? 1;
                      final isPassed = _currentStopSequence != null && seq < _currentStopSequence!;
                      final isCurrent = _currentStopSequence != null && seq == _currentStopSequence;

                      return Padding(
                        padding: const EdgeInsets.symmetric(vertical: 6.0),
                        child: Row(
                          children: [
                            Container(
                              width: 24,
                              height: 24,
                              decoration: BoxDecoration(
                                shape: BoxShape.circle,
                                color: isCurrent
                                    ? const Color(0xFF10B981)
                                    : (isPassed ? const Color(0xFF10B981).withOpacity(0.3) : const Color(0xFF1F2937)),
                                border: Border.all(
                                  color: isCurrent ? Colors.white : const Color(0xFF374151),
                                ),
                              ),
                              child: Center(
                                child: isPassed
                                    ? const Icon(Icons.check, size: 14, color: Colors.white)
                                    : Text(
                                        '$seq',
                                        style: TextStyle(
                                          color: isCurrent ? Colors.white : Colors.grey,
                                          fontSize: 11,
                                          fontWeight: FontWeight.bold,
                                        ),
                                      ),
                              ),
                            ),
                            const SizedBox(width: 12),
                            Expanded(
                              child: Text(
                                stop['stopName'] ?? 'Stop $seq',
                                style: TextStyle(
                                  color: isCurrent ? Colors.white : (isPassed ? Colors.grey : Colors.white70),
                                  fontWeight: isCurrent ? FontWeight.bold : FontWeight.normal,
                                  fontSize: 13,
                                ),
                              ),
                            ),
                            if (stop['stageFareInr'] != null)
                              Text(
                                '₹${stop['stageFareInr']}',
                                style: const TextStyle(color: Colors.grey, fontSize: 12),
                              ),
                          ],
                        ),
                      );
                    }).toList(),
                  ],
                ),
              ),
          ],
        ),
      ),
    );
  }
}

class _MapGridPainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = const Color(0xFF1E293B).withOpacity(0.4)
      ..strokeWidth = 1.0;

    const step = 30.0;
    for (double x = 0; x < size.width; x += step) {
      canvas.drawLine(Offset(x, 0), Offset(x, size.height), paint);
    }
    for (double y = 0; y < size.height; y += step) {
      canvas.drawLine(Offset(0, y), Offset(size.width, y), paint);
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}
