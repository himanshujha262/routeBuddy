import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import '../../../core/constants/api_constants.dart';
import '../../../core/network/api_client.dart';
import '../../../core/services/driver_telemetry_service.dart';
import '../../route_management/presentation/route_creation_screen.dart';
import '../../vehicle/presentation/vehicle_registration_screen.dart';
import 'create_trip_screen.dart';

class DriverTripScreen extends StatefulWidget {
  const DriverTripScreen({Key? key}) : super(key: key);

  @override
  State<DriverTripScreen> createState() => _DriverTripScreenState();
}

class _DriverTripScreenState extends State<DriverTripScreen> {
  final _telemetryService = DriverTelemetryService();
  bool _isOnline = false;
  String _kycStatus = 'PENDING';
  dynamic _driverProfile;
  dynamic _activeTrip;
  bool _isLoading = true;
  bool _isActionLoading = false;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _telemetryService.addListener(_onTelemetryChanged);
    _loadDriverData();
  }

  @override
  void dispose() {
    _telemetryService.removeListener(_onTelemetryChanged);
    super.dispose();
  }

  void _onTelemetryChanged() {
    if (mounted) setState(() {});
  }

  Future<void> _loadDriverData() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      // 1. Fetch Driver Profile
      final profileRes = await ApiClient().dio.get(ApiConstants.driverMe);
      if (profileRes.statusCode == 200 && profileRes.data['data'] != null) {
        final profile = profileRes.data['data'];
        setState(() {
          _driverProfile = profile;
          _isOnline = profile['onlineStatus'] ?? false;
          _kycStatus = profile['kycStatus'] ?? 'PENDING';
        });
      }

      // 2. Fetch Active Trip from Spring Boot backend
      try {
        final activeTripRes = await ApiClient().dio.get(ApiConstants.driverActiveTrip);
        if (activeTripRes.statusCode == 200 && activeTripRes.data['data'] != null) {
          setState(() {
            _activeTrip = activeTripRes.data['data'];
          });
        } else {
          setState(() => _activeTrip = null);
        }
      } catch (_) {
        setState(() => _activeTrip = null);
      }
    } catch (e) {
      // Driver profile might need onboarding
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  Future<void> _toggleOnlineStatus(bool online) async {
    try {
      final res = await ApiClient().dio.put(
        ApiConstants.driverStatus,
        queryParameters: {'online': online},
      );
      if (res.statusCode == 200) {
        setState(() => _isOnline = online);
      }
    } on DioException catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(e.response?.data?['message'] ?? 'Failed to update online status. Check KYC status.')),
      );
    }
  }

  Future<void> _updateTripStatus(String actionEndpoint) async {
    if (_activeTrip == null) return;
    final tripId = _activeTrip['id'];

    setState(() => _isActionLoading = true);

    try {
      final res = await ApiClient().dio.put('${ApiConstants.trips}/$tripId/$actionEndpoint');
      if (res.statusCode == 200) {
        if (actionEndpoint == 'start-boarding' || actionEndpoint == 'start-trip') {
          final driverId = _driverProfile?['id'] ?? _activeTrip['driverId'];
          if (driverId != null) {
            _telemetryService.startTracking(tripId: tripId, driverId: driverId);
          }
        } else if (actionEndpoint == 'complete' || actionEndpoint == 'cancel') {
          _telemetryService.stopTracking();
        }
        await _loadDriverData();
      }
    } on DioException catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(e.response?.data?['message'] ?? 'Trip status update failed')),
      );
    } finally {
      if (mounted) setState(() => _isActionLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final int totalSeats = _activeTrip?['totalSeats'] ?? 3;
    final int availableSeats = _activeTrip?['availableSeats'] ?? 3;
    final int occupiedSeats = totalSeats - availableSeats;
    final String tripStatus = _activeTrip?['status'] ?? 'NONE';

    return Scaffold(
      backgroundColor: const Color(0xFF0B0F19),
      appBar: AppBar(
        backgroundColor: const Color(0xFF111827),
        elevation: 0,
        title: const Text('Driver Dashboard', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18, color: Colors.white)),
        actions: [
          Row(
            children: [
              Text(_isOnline ? 'ONLINE' : 'OFFLINE', style: TextStyle(color: _isOnline ? const Color(0xFF10B981) : Colors.grey, fontSize: 11, fontWeight: FontWeight.bold)),
              Switch(
                value: _isOnline,
                activeColor: const Color(0xFF10B981),
                onChanged: _toggleOnlineStatus,
              ),
            ],
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator(color: Color(0xFF10B981)))
          : RefreshIndicator(
              onRefresh: _loadDriverData,
              color: const Color(0xFF10B981),
              child: SingleChildScrollView(
                physics: const AlwaysScrollableScrollPhysics(),
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    // KYC & Driver Status Banner
                    Container(
                      padding: const EdgeInsets.all(14),
                      decoration: BoxDecoration(
                        color: const Color(0xFF182234),
                        borderRadius: BorderRadius.circular(14),
                        border: Border.all(color: const Color(0xFF1F2937)),
                      ),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Row(
                            children: [
                              CircleAvatar(
                                backgroundColor: const Color(0xFF10B981).withOpacity(0.2),
                                child: const Icon(Icons.badge_outlined, color: Color(0xFF10B981)),
                              ),
                              const SizedBox(width: 12),
                              Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    _driverProfile?['licenseNumber'] ?? 'DL Profile',
                                    style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.white, fontSize: 14),
                                  ),
                                  Text(
                                    'KYC: $_kycStatus',
                                    style: TextStyle(
                                      color: _kycStatus == 'APPROVED' ? const Color(0xFF10B981) : Colors.amber,
                                      fontSize: 12,
                                      fontWeight: FontWeight.w600,
                                    ),
                                  ),
                                ],
                              ),
                            ],
                          ),
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                            decoration: BoxDecoration(
                              color: const Color(0xFF10B981).withOpacity(0.15),
                              borderRadius: BorderRadius.circular(6),
                            ),
                            child: Text(
                              '${_driverProfile?['totalTripsCompleted'] ?? 0} Trips Done',
                              style: const TextStyle(color: Color(0xFF10B981), fontSize: 11, fontWeight: FontWeight.bold),
                            ),
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(height: 16),

                    // Active Trip Management Card
                    if (_activeTrip != null) ...[
                      Container(
                        padding: const EdgeInsets.all(16),
                        decoration: BoxDecoration(
                          color: const Color(0xFF182234),
                          borderRadius: BorderRadius.circular(16),
                          border: Border.all(color: const Color(0xFF10B981).withOpacity(0.4)),
                        ),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                const Text('CURRENT ACTIVE TRIP', style: TextStyle(color: Color(0xFF10B981), fontSize: 11, fontWeight: FontWeight.bold, letterSpacing: 1.1)),
                                Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                                  decoration: BoxDecoration(
                                    color: const Color(0xFF10B981).withOpacity(0.2),
                                    borderRadius: BorderRadius.circular(4),
                                  ),
                                  child: Text(tripStatus, style: const TextStyle(color: Color(0xFF10B981), fontSize: 10, fontWeight: FontWeight.bold)),
                                ),
                              ],
                            ),
                            const SizedBox(height: 12),
                            Text(
                              'Vehicle: ${_activeTrip['vehiclePlateNumber'] ?? 'Assigned Vehicle'}',
                              style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 16),
                            ),
                            const SizedBox(height: 4),
                            Text('Trip ID: ${_activeTrip['id'].toString().substring(0, 8)}...', style: const TextStyle(color: Colors.grey, fontSize: 12)),
                            const Divider(color: Color(0xFF1F2937), height: 24),

                            // Occupancy Gauge
                            Row(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                Text(
                                  '$occupiedSeats',
                                  style: const TextStyle(color: Color(0xFF10B981), fontSize: 40, fontWeight: FontWeight.bold),
                                ),
                                Text(
                                  ' / $totalSeats Seats Booked',
                                  style: const TextStyle(color: Colors.grey, fontSize: 20),
                                ),
                              ],
                            ),
                            Center(
                              child: Text(
                                '$availableSeats Available for Boarding',
                                style: const TextStyle(color: Colors.white70, fontSize: 12),
                              ),
                            ),
                            const SizedBox(height: 16),

                            // Dynamic Trip State Actions
                            if (tripStatus == 'PUBLISHED')
                              SizedBox(
                                width: double.infinity,
                                child: ElevatedButton.icon(
                                  onPressed: _isActionLoading ? null : () => _updateTripStatus('start-boarding'),
                                  icon: const Icon(Icons.people_outline),
                                  label: const Text('Open Vehicle for Boarding'),
                                  style: ElevatedButton.styleFrom(
                                    backgroundColor: const Color(0xFF10B981),
                                    foregroundColor: Colors.white,
                                    padding: const EdgeInsets.symmetric(vertical: 14),
                                  ),
                                ),
                              )
                            else if (tripStatus == 'BOARDING')
                              SizedBox(
                                width: double.infinity,
                                child: ElevatedButton.icon(
                                  onPressed: _isActionLoading ? null : () => _updateTripStatus('start-trip'),
                                  icon: const Icon(Icons.navigation),
                                  label: const Text('Depart Stand (In Transit)'),
                                  style: ElevatedButton.styleFrom(
                                    backgroundColor: Colors.blueAccent,
                                    foregroundColor: Colors.white,
                                    padding: const EdgeInsets.symmetric(vertical: 14),
                                  ),
                                ),
                              )
                            else if (tripStatus == 'IN_TRANSIT')
                              SizedBox(
                                width: double.infinity,
                                child: ElevatedButton.icon(
                                  onPressed: _isActionLoading ? null : () => _updateTripStatus('complete'),
                                  icon: const Icon(Icons.check_circle_outline),
                                  label: const Text('Complete Trip & Credit Ledger'),
                                  style: ElevatedButton.styleFrom(
                                    backgroundColor: const Color(0xFF10B981),
                                    foregroundColor: Colors.white,
                                    padding: const EdgeInsets.symmetric(vertical: 14),
                                  ),
                                ),
                              ),
                          ],
                        ),
                      ),
                      const SizedBox(height: 16),

                      // Live GPS Telemetry Broadcast Panel
                      Container(
                        padding: const EdgeInsets.all(16),
                        decoration: BoxDecoration(
                          color: const Color(0xFF182234),
                          borderRadius: BorderRadius.circular(16),
                          border: Border.all(
                            color: _telemetryService.state.isBroadcasting
                                ? const Color(0xFF10B981)
                                : const Color(0xFF1F2937),
                          ),
                        ),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                Row(
                                  children: [
                                    Container(
                                      width: 10,
                                      height: 10,
                                      decoration: BoxDecoration(
                                        shape: BoxShape.circle,
                                        color: _telemetryService.state.isBroadcasting
                                            ? const Color(0xFF10B981)
                                            : Colors.grey,
                                      ),
                                    ),
                                    const SizedBox(width: 8),
                                    const Text(
                                      'LIVE GPS STREAMING',
                                      style: TextStyle(
                                        color: Colors.white,
                                        fontWeight: FontWeight.bold,
                                        fontSize: 13,
                                      ),
                                    ),
                                  ],
                                ),
                                Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                                  decoration: BoxDecoration(
                                    color: _telemetryService.state.isConnected
                                        ? const Color(0xFF10B981).withOpacity(0.15)
                                        : Colors.amber.withOpacity(0.15),
                                    borderRadius: BorderRadius.circular(6),
                                  ),
                                  child: Text(
                                    _telemetryService.state.isConnected
                                        ? 'WebSocket Live'
                                        : (_telemetryService.state.isReconnecting
                                            ? 'Reconnecting...'
                                            : 'Disconnected'),
                                    style: TextStyle(
                                      color: _telemetryService.state.isConnected
                                          ? const Color(0xFF10B981)
                                          : Colors.amber,
                                      fontSize: 11,
                                      fontWeight: FontWeight.bold,
                                    ),
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 12),
                            Row(
                              mainAxisAlignment: MainAxisAlignment.spaceAround,
                              children: [
                                Column(
                                  children: [
                                    Text(
                                      '${_telemetryService.state.currentSpeedKmph.toStringAsFixed(1)}',
                                      style: const TextStyle(
                                        color: Color(0xFF10B981),
                                        fontSize: 22,
                                        fontWeight: FontWeight.bold,
                                      ),
                                    ),
                                    const Text('Speed (km/h)', style: TextStyle(color: Colors.grey, fontSize: 10)),
                                  ],
                                ),
                                Column(
                                  children: [
                                    Text(
                                      '${_telemetryService.state.pingCount}',
                                      style: const TextStyle(
                                        color: Colors.white,
                                        fontSize: 22,
                                        fontWeight: FontWeight.bold,
                                      ),
                                    ),
                                    const Text('Pings Streamed', style: TextStyle(color: Colors.grey, fontSize: 10)),
                                  ],
                                ),
                                Column(
                                  children: [
                                    Text(
                                      '±${_telemetryService.state.accuracyMeters.toStringAsFixed(0)}m',
                                      style: const TextStyle(
                                        color: Colors.white70,
                                        fontSize: 22,
                                        fontWeight: FontWeight.bold,
                                      ),
                                    ),
                                    const Text('GPS Accuracy', style: TextStyle(color: Colors.grey, fontSize: 10)),
                                  ],
                                ),
                              ],
                            ),
                            const SizedBox(height: 12),
                            SizedBox(
                              width: double.infinity,
                              child: OutlinedButton.icon(
                                onPressed: () {
                                  if (_telemetryService.state.isBroadcasting) {
                                    _telemetryService.stopTracking();
                                  } else {
                                    final driverId = _driverProfile?['id'] ?? _activeTrip['driverId'];
                                    if (driverId != null) {
                                      _telemetryService.startTracking(
                                        tripId: _activeTrip['id'],
                                        driverId: driverId,
                                      );
                                    }
                                  }
                                },
                                icon: Icon(
                                  _telemetryService.state.isBroadcasting
                                      ? Icons.stop_circle_outlined
                                      : Icons.play_circle_outline,
                                  color: _telemetryService.state.isBroadcasting
                                      ? Colors.redAccent
                                      : const Color(0xFF10B981),
                                ),
                                label: Text(
                                  _telemetryService.state.isBroadcasting
                                      ? 'Pause Live GPS Telemetry'
                                      : 'Start Live GPS Broadcasting',
                                  style: TextStyle(
                                    color: _telemetryService.state.isBroadcasting
                                        ? Colors.redAccent
                                        : const Color(0xFF10B981),
                                  ),
                                ),
                                style: OutlinedButton.styleFrom(
                                  side: BorderSide(
                                    color: _telemetryService.state.isBroadcasting
                                        ? Colors.redAccent.withOpacity(0.5)
                                        : const Color(0xFF10B981).withOpacity(0.5),
                                  ),
                                ),
                              ),
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(height: 20),
                    ] else ...[
                      // No Active Trip Banner
                      Container(
                        padding: const EdgeInsets.all(24),
                        decoration: BoxDecoration(
                          color: const Color(0xFF182234),
                          borderRadius: BorderRadius.circular(16),
                          border: Border.all(color: const Color(0xFF1F2937)),
                        ),
                        child: Column(
                          children: [
                            const Icon(Icons.electric_rickshaw, size: 48, color: Colors.grey),
                            const SizedBox(height: 12),
                            const Text('No Active Trip Scheduled', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 16)),
                            const SizedBox(height: 6),
                            const Text('Dispatch a new trip on your corridor to accept passenger bookings.', textAlign: TextAlign.center, style: TextStyle(color: Colors.grey, fontSize: 12)),
                            const SizedBox(height: 16),
                            ElevatedButton.icon(
                              onPressed: () async {
                                final res = await Navigator.of(context).push(
                                  MaterialPageRoute(builder: (_) => const CreateTripScreen()),
                                );
                                if (res == true) _loadDriverData();
                              },
                              icon: const Icon(Icons.add_road),
                              label: const Text('Dispatch New Trip'),
                              style: ElevatedButton.styleFrom(
                                backgroundColor: const Color(0xFF10B981),
                                foregroundColor: Colors.white,
                              ),
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(height: 20),
                    ],

                    // Quick Management Actions
                    const Text('Driver Transit Tools', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 16)),
                    const SizedBox(height: 10),

                    Row(
                      children: [
                        Expanded(
                          child: _buildQuickActionCard(
                            icon: Icons.add_road,
                            title: 'Dispatch Trip',
                            subtitle: 'Publish corridor ride',
                            onTap: () async {
                              final res = await Navigator.of(context).push(
                                MaterialPageRoute(builder: (_) => const CreateTripScreen()),
                              );
                              if (res == true) _loadDriverData();
                            },
                          ),
                        ),
                        const SizedBox(width: 10),
                        Expanded(
                          child: _buildQuickActionCard(
                            icon: Icons.alt_route,
                            title: 'Add Route',
                            subtitle: 'Create corridor & stops',
                            onTap: () {
                              Navigator.of(context).push(
                                MaterialPageRoute(builder: (_) => const RouteCreationScreen()),
                              );
                            },
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 10),

                    _buildQuickActionCard(
                      icon: Icons.directions_car_filled_outlined,
                      title: 'Register Vehicle',
                      subtitle: 'Add 3-Wheeler Auto / E-Rickshaw to your profile',
                      onTap: () {
                        Navigator.of(context).push(
                          MaterialPageRoute(builder: (_) => const VehicleRegistrationScreen()),
                        );
                      },
                    ),
                  ],
                ),
              ),
            ),
    );
  }

  Widget _buildQuickActionCard({
    required IconData icon,
    required String title,
    required String subtitle,
    required VoidCallback onTap,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          color: const Color(0xFF182234),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: const Color(0xFF1F2937)),
        ),
        child: Row(
          children: [
            CircleAvatar(
              radius: 18,
              backgroundColor: const Color(0xFF10B981).withOpacity(0.15),
              child: Icon(icon, color: const Color(0xFF10B981), size: 20),
            ),
            const SizedBox(width: 10),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(title, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 13)),
                  Text(subtitle, style: const TextStyle(color: Colors.grey, fontSize: 10)),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
