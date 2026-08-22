import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:qr_flutter/qr_flutter.dart';
import '../../../core/constants/api_constants.dart';
import '../../../core/network/api_client.dart';
import '../../tracking/presentation/live_trip_tracking_screen.dart';

class BoardingPassScreen extends StatefulWidget {
  final dynamic booking;

  const BoardingPassScreen({Key? key, this.booking}) : super(key: key);

  @override
  State<BoardingPassScreen> createState() => _BoardingPassScreenState();
}

class _BoardingPassScreenState extends State<BoardingPassScreen> {
  dynamic _bookingData;
  bool _isLoading = false;
  String? _errorMessage;
  bool _isCancelling = false;

  @override
  void initState() {
    super.initState();
    if (widget.booking != null) {
      _bookingData = widget.booking;
    } else {
      _fetchActiveBooking();
    }
  }

  Future<void> _fetchActiveBooking() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final response = await ApiClient().dio.get(
        ApiConstants.myBookings,
        queryParameters: {'page': 0, 'size': 1},
      );

      if (response.statusCode == 200 && response.data['data'] != null) {
        final content = response.data['data']['content'] as List<dynamic>?;
        if (content != null && content.isNotEmpty) {
          setState(() {
            _bookingData = content.first;
          });
        } else {
          setState(() {
            _bookingData = null;
          });
        }
      }
    } catch (e) {
      setState(() => _errorMessage = 'Failed to load boarding pass');
    } finally {
      setState(() => _isLoading = false);
    }
  }

  Future<void> _handleCancelBooking() async {
    if (_bookingData == null) return;
    final bookingId = _bookingData['id'];

    setState(() => _isCancelling = true);

    try {
      final response = await ApiClient().dio.put(
        '${ApiConstants.cancelBooking}/$bookingId/cancel',
        data: {'reason': 'Cancelled by passenger'},
      );

      if (response.statusCode == 200) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Booking cancelled successfully and seats released.')),
        );
        await _fetchActiveBooking();
      }
    } on DioException catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(e.response?.data?['message'] ?? 'Failed to cancel booking')),
      );
    } finally {
      if (mounted) setState(() => _isCancelling = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0B0F19),
      appBar: AppBar(
        backgroundColor: const Color(0xFF111827),
        elevation: 0,
        title: const Text('Digital Boarding Pass', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18, color: Colors.white)),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh, color: Colors.white),
            onPressed: _fetchActiveBooking,
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator(color: Color(0xFF10B981)))
          : _bookingData == null
              ? Center(
                  child: Padding(
                    padding: const EdgeInsets.all(32.0),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: const [
                        Icon(Icons.qr_code_2_outlined, size: 64, color: Colors.grey),
                        SizedBox(height: 16),
                        Text(
                          'No Active Boarding Pass',
                          style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 18),
                        ),
                        SizedBox(height: 8),
                        Text(
                          'Book a shared auto to get your digital QR boarding pass.',
                          textAlign: TextAlign.center,
                          style: TextStyle(color: Colors.grey, fontSize: 13),
                        ),
                      ],
                    ),
                  ),
                )
              : SingleChildScrollView(
                  padding: const EdgeInsets.all(16.0),
                  child: Column(
                    children: [
                      // Digital QR Ticket Card
                      Container(
                        padding: const EdgeInsets.all(20),
                        decoration: BoxDecoration(
                          color: const Color(0xFF182234),
                          borderRadius: BorderRadius.circular(20),
                          border: Border.all(color: const Color(0xFF10B981).withOpacity(0.5)),
                        ),
                        child: Column(
                          children: [
                            Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    const Text('BOOKING CODE', style: TextStyle(color: Colors.grey, fontSize: 10, fontWeight: FontWeight.bold)),
                                    Text(
                                      _bookingData['bookingCode'] ?? 'RB-XXXX',
                                      style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 16),
                                    ),
                                  ],
                                ),
                                Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                                  decoration: BoxDecoration(
                                    color: const Color(0xFF10B981).withOpacity(0.2),
                                    borderRadius: BorderRadius.circular(8),
                                  ),
                                  child: Text(
                                    _bookingData['bookingStatus'] ?? 'CONFIRMED',
                                    style: const TextStyle(color: Color(0xFF10B981), fontWeight: FontWeight.bold, fontSize: 12),
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 20),

                            // Dynamic QR Code
                            Container(
                              padding: const EdgeInsets.all(12),
                              decoration: BoxDecoration(
                                color: Colors.white,
                                borderRadius: BorderRadius.circular(16),
                              ),
                              child: QrImageView(
                                data: _bookingData['qrToken'] ?? _bookingData['bookingCode'] ?? 'ROUTBUDDY',
                                version: QrVersions.auto,
                                size: 180.0,
                                foregroundColor: Colors.black,
                              ),
                            ),
                            const SizedBox(height: 16),

                            // 6-digit OTP Code
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                              decoration: BoxDecoration(
                                color: const Color(0xFF111827),
                                borderRadius: BorderRadius.circular(10),
                                border: Border.all(color: const Color(0xFF1F2937)),
                              ),
                              child: Row(
                                mainAxisSize: MainAxisSize.min,
                                children: [
                                  const Text('Manual Boarding OTP: ', style: TextStyle(color: Colors.grey, fontSize: 12)),
                                  Text(
                                    _bookingData['otpCode'] ?? '------',
                                    style: const TextStyle(color: Color(0xFF10B981), fontWeight: FontWeight.bold, fontSize: 18, letterSpacing: 2.0),
                                  ),
                                ],
                              ),
                            ),
                            const Divider(color: Color(0xFF1F2937), height: 28),

                            // Stops Overview
                            Row(
                              children: [
                                const Icon(Icons.my_location, color: Color(0xFF10B981), size: 18),
                                const SizedBox(width: 10),
                                Expanded(
                                  child: Text(
                                    'Pickup: ${_bookingData['pickupStopName'] ?? 'Origin'}',
                                    style: const TextStyle(color: Colors.white, fontSize: 13, fontWeight: FontWeight.w600),
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 8),
                            Row(
                              children: [
                                const Icon(Icons.location_on, color: Colors.amber, size: 18),
                                const SizedBox(width: 10),
                                Expanded(
                                  child: Text(
                                    'Dropoff: ${_bookingData['dropoffStopName'] ?? 'Destination'}',
                                    style: const TextStyle(color: Colors.white, fontSize: 13, fontWeight: FontWeight.w600),
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 12),

                            Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                Text('Seats Reserved: ${_bookingData['seatCount'] ?? 1}', style: const TextStyle(color: Colors.grey, fontSize: 12)),
                                Text('Fare Paid: ₹${(_bookingData['fareAmountInr'] as num?)?.toStringAsFixed(0) ?? '0'}', style: const TextStyle(color: Color(0xFF10B981), fontWeight: FontWeight.bold, fontSize: 14)),
                              ],
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(height: 16),

                      // Live Tracking Button
                      SizedBox(
                        width: double.infinity,
                        child: ElevatedButton.icon(
                          onPressed: () {
                            Navigator.of(context).push(
                              MaterialPageRoute(
                                builder: (_) => LiveTripTrackingScreen(
                                  trip: {
                                    'id': _bookingData['tripId'],
                                    'tripId': _bookingData['tripId'],
                                    'status': _bookingData['bookingStatus'],
                                    'vehiclePlateNumber': 'Shared Auto',
                                  },
                                  route: {
                                    'name': '${_bookingData['pickupStopName'] ?? 'Origin'} → ${_bookingData['dropoffStopName'] ?? 'Destination'}',
                                  },
                                ),
                              ),
                            );
                          },
                          icon: const Icon(Icons.navigation_outlined),
                          label: const Text('Track Live Vehicle GPS'),
                          style: ElevatedButton.styleFrom(
                            backgroundColor: const Color(0xFF10B981),
                            foregroundColor: Colors.white,
                            padding: const EdgeInsets.symmetric(vertical: 14),
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                          ),
                        ),
                      ),
                      const SizedBox(height: 12),

                      // Cancel Booking Action
                      if (_bookingData['bookingStatus'] == 'CONFIRMED')
                        SizedBox(
                          width: double.infinity,
                          child: OutlinedButton.icon(
                            onPressed: _isCancelling
                                ? null
                                : () {
                                    showDialog(
                                      context: context,
                                      builder: (ctx) => AlertDialog(
                                        backgroundColor: const Color(0xFF182234),
                                        title: const Text('Cancel Booking?', style: TextStyle(color: Colors.white)),
                                        content: const Text(
                                          'Are you sure you want to cancel this booking? Reserved seats will be released.',
                                          style: TextStyle(color: Colors.white70),
                                        ),
                                        actions: [
                                          TextButton(
                                            onPressed: () => Navigator.of(ctx).pop(),
                                            child: const Text('Keep Booking', style: TextStyle(color: Colors.grey)),
                                          ),
                                          ElevatedButton(
                                            onPressed: () {
                                              Navigator.of(ctx).pop();
                                              _handleCancelBooking();
                                            },
                                            style: ElevatedButton.styleFrom(backgroundColor: Colors.redAccent),
                                            child: const Text('Confirm Cancel', style: TextStyle(color: Colors.white)),
                                          ),
                                        ],
                                      ),
                                    );
                                  },
                            icon: const Icon(Icons.cancel_outlined, color: Colors.redAccent),
                            label: _isCancelling
                                ? const SizedBox(
                                    height: 16,
                                    width: 16,
                                    child: CircularProgressIndicator(strokeWidth: 2, color: Colors.redAccent),
                                  )
                                : const Text('Cancel Booking & Release Seats', style: TextStyle(color: Colors.redAccent)),
                            style: OutlinedButton.styleFrom(
                              side: const BorderSide(color: Colors.redAccent),
                              padding: const EdgeInsets.symmetric(vertical: 14),
                              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                            ),
                          ),
                        ),
                    ],
                  ),
                ),
    );
  }
}
