import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import '../../../core/constants/api_constants.dart';
import '../../../core/network/api_client.dart';

class DriverEarningsScreen extends StatefulWidget {
  const DriverEarningsScreen({Key? key}) : super(key: key);

  @override
  State<DriverEarningsScreen> createState() => _DriverEarningsScreenState();
}

class _DriverEarningsScreenState extends State<DriverEarningsScreen> {
  dynamic _driverProfile;
  List<dynamic> _trips = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _fetchEarningsData();
  }

  Future<void> _fetchEarningsData() async {
    setState(() => _isLoading = true);

    try {
      final profileRes = await ApiClient().dio.get(ApiConstants.driverMe);
      if (profileRes.statusCode == 200) {
        _driverProfile = profileRes.data['data'];
      }

      try {
        final activeTripRes = await ApiClient().dio.get(ApiConstants.driverActiveTrip);
        if (activeTripRes.statusCode == 200 && activeTripRes.data['data'] != null) {
          _trips = [activeTripRes.data['data']];
        }
      } catch (_) {}
    } catch (_) {}
    finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final int totalTrips = _driverProfile?['totalTripsCompleted'] ?? _trips.length;
    final double rating = (_driverProfile?['rating'] as num?)?.toDouble() ?? 4.9;
    final double totalEstimatedEarnings = _trips.fold(0.0, (acc, t) {
      final seats = ((t['totalSeats'] ?? 3) - (t['availableSeats'] ?? 0)) as int;
      final fare = (t['farePerSeatInr'] as num?)?.toDouble() ?? 20.0;
      return acc + (seats * fare);
    });

    return Scaffold(
      backgroundColor: const Color(0xFF0B0F19),
      appBar: AppBar(
        backgroundColor: const Color(0xFF111827),
        elevation: 0,
        title: const Text('Earnings & Ledger', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18, color: Colors.white)),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh, color: Colors.white),
            onPressed: _fetchEarningsData,
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator(color: Color(0xFF10B981)))
          : RefreshIndicator(
              onRefresh: _fetchEarningsData,
              color: const Color(0xFF10B981),
              child: SingleChildScrollView(
                physics: const AlwaysScrollableScrollPhysics(),
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    // Earnings Hero Card
                    Container(
                      padding: const EdgeInsets.all(20),
                      decoration: BoxDecoration(
                        gradient: const LinearGradient(
                          colors: [Color(0xFF10B981), Color(0xFF059669)],
                          begin: Alignment.topLeft,
                          end: Alignment.bottomRight,
                        ),
                        borderRadius: BorderRadius.circular(20),
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Text('TOTAL ESTIMATED EARNINGS', style: TextStyle(color: Colors.white70, fontSize: 11, fontWeight: FontWeight.bold, letterSpacing: 1.1)),
                          const SizedBox(height: 8),
                          Text(
                            '₹${totalEstimatedEarnings.toStringAsFixed(0)}',
                            style: const TextStyle(color: Colors.white, fontSize: 36, fontWeight: FontWeight.bold),
                          ),
                          const SizedBox(height: 16),
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Text('$totalTrips Completed Trips', style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w600)),
                              Text('$rating ★ Rating', style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w600)),
                            ],
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(height: 24),

                    const Text('Recent Corridor Trips', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 16)),
                    const SizedBox(height: 12),

                    if (_trips.isEmpty)
                      Container(
                        padding: const EdgeInsets.all(32),
                        alignment: Alignment.center,
                        child: const Text('No trips recorded yet.', style: TextStyle(color: Colors.grey)),
                      )
                    else
                      ..._trips.map((t) {
                        final bookedSeats = ((t['totalSeats'] ?? 3) - (t['availableSeats'] ?? 0)) as int;
                        final fare = (t['farePerSeatInr'] as num?)?.toDouble() ?? 20.0;
                        final tripEarnings = bookedSeats * fare;

                        return Container(
                          margin: const EdgeInsets.only(bottom: 10),
                          padding: const EdgeInsets.all(14),
                          decoration: BoxDecoration(
                            color: const Color(0xFF182234),
                            borderRadius: BorderRadius.circular(12),
                            border: Border.all(color: const Color(0xFF1F2937)),
                          ),
                          child: Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    t['vehiclePlateNumber'] ?? 'Trip',
                                    style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 14),
                                  ),
                                  const SizedBox(height: 4),
                                  Text(
                                    'Status: ${t['status']} • $bookedSeats seats booked',
                                    style: const TextStyle(color: Colors.grey, fontSize: 11),
                                  ),
                                ],
                              ),
                              Text(
                                '+₹${tripEarnings.toStringAsFixed(0)}',
                                style: const TextStyle(color: Color(0xFF10B981), fontWeight: FontWeight.bold, fontSize: 16),
                              ),
                            ],
                          ),
                        );
                      }).toList(),
                  ],
                ),
              ),
            ),
    );
  }
}
