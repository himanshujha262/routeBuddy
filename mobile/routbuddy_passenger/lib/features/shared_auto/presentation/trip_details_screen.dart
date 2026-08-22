import 'dart:math';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import '../../../core/constants/api_constants.dart';
import '../../../core/network/api_client.dart';
import 'boarding_pass_screen.dart';
import '../../tracking/presentation/live_trip_tracking_screen.dart';

class TripDetailsScreen extends StatefulWidget {
  final dynamic trip;
  final dynamic route;
  final dynamic pickupStop;
  final dynamic dropoffStop;

  const TripDetailsScreen({
    Key? key,
    required this.trip,
    required this.route,
    required this.pickupStop,
    required this.dropoffStop,
  }) : super(key: key);

  @override
  State<TripDetailsScreen> createState() => _TripDetailsScreenState();
}

class _TripDetailsScreenState extends State<TripDetailsScreen> {
  int _selectedSeats = 1;
  String _paymentMethod = 'UPI_INTENT';
  bool _isBooking = false;
  String? _errorMessage;

  double get _farePerSeat {
    final pickupFare = (widget.pickupStop?['stageFareInr'] as num?)?.toDouble() ?? 0.0;
    final dropoffFare = (widget.dropoffStop?['stageFareInr'] as num?)?.toDouble() ?? 0.0;
    final tripBaseFare = (widget.trip?['farePerSeatInr'] as num?)?.toDouble() ?? 20.0;

    final diff = (dropoffFare - pickupFare).abs();
    return diff > 0 ? max(diff, tripBaseFare) : tripBaseFare;
  }

  double get _totalFare => _farePerSeat * _selectedSeats;
  int get _maxAvailableSeats => (widget.trip['availableSeats'] as num?)?.toInt() ?? 1;

  Future<void> _handleConfirmBooking() async {
    setState(() {
      _isBooking = true;
      _errorMessage = null;
    });

    final idempotencyKey = 'pass-book-${DateTime.now().millisecondsSinceEpoch}-${Random().nextInt(99999)}';

    try {
      final response = await ApiClient().dio.post(
        ApiConstants.bookings,
        data: {
          'tripId': widget.trip['tripId'] ?? widget.trip['id'],
          'pickupStopId': widget.pickupStop['id'],
          'dropoffStopId': widget.dropoffStop['id'],
          'seatCount': _selectedSeats,
          'paymentMethod': _paymentMethod,
          'idempotencyKey': idempotencyKey,
        },
      );

      if (response.statusCode == 201 && response.data['data'] != null) {
        final bookingData = response.data['data'];
        if (!mounted) return;

        Navigator.of(context).pushReplacement(
          MaterialPageRoute(
            builder: (_) => BoardingPassScreen(booking: bookingData),
          ),
        );
      }
    } on DioException catch (e) {
      String msg = 'Booking failed. Please try again.';
      if (e.response?.data != null && e.response?.data['message'] != null) {
        msg = e.response!.data['message'];
      }
      setState(() => _errorMessage = msg);
    } catch (e) {
      setState(() => _errorMessage = 'An unexpected error occurred during booking.');
    } finally {
      if (mounted) setState(() => _isBooking = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final vehiclePlate = widget.trip['vehiclePlateNumber'] ?? 'Shared Auto';
    final routeName = widget.route?['name'] ?? 'Transit Route';
    final pickupName = widget.pickupStop?['stopName'] ?? 'Pickup';
    final dropoffName = widget.dropoffStop?['stopName'] ?? 'Dropoff';

    return Scaffold(
      backgroundColor: const Color(0xFF0B0F19),
      appBar: AppBar(
        backgroundColor: const Color(0xFF111827),
        elevation: 0,
        title: const Text('Trip Details & Seats', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18, color: Colors.white)),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back, color: Colors.white),
          onPressed: () => Navigator.of(context).pop(),
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Route & Stop Overview Card
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
                  Text(
                    routeName,
                    style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: Colors.white),
                  ),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      const Icon(Icons.my_location, color: Color(0xFF10B981), size: 18),
                      const SizedBox(width: 10),
                      Expanded(
                        child: Text(
                          'From: $pickupName',
                          style: const TextStyle(color: Colors.white70, fontSize: 13),
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
                          'To: $dropoffName',
                          style: const TextStyle(color: Colors.white70, fontSize: 13),
                        ),
                      ),
                    ],
                  ),
                  const Divider(color: Color(0xFF1F2937), height: 24),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text('Vehicle: $vehiclePlate', style: const TextStyle(color: Colors.grey, fontSize: 12)),
                      Text('Available: $_maxAvailableSeats seats', style: const TextStyle(color: Color(0xFF10B981), fontWeight: FontWeight.bold, fontSize: 12)),
                    ],
                  ),
                  const SizedBox(height: 12),
                  SizedBox(
                    width: double.infinity,
                    child: OutlinedButton.icon(
                      onPressed: () {
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (_) => LiveTripTrackingScreen(
                              trip: widget.trip,
                              route: widget.route,
                            ),
                          ),
                        );
                      },
                      icon: const Icon(Icons.navigation, color: Color(0xFF10B981), size: 16),
                      label: const Text('View Live Vehicle Location & Map', style: TextStyle(color: Color(0xFF10B981))),
                      style: OutlinedButton.styleFrom(
                        side: BorderSide(color: const Color(0xFF10B981).withOpacity(0.5)),
                        padding: const EdgeInsets.symmetric(vertical: 10),
                      ),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),

            // Interactive Seat Selector
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
                    'SELECT NUMBER OF SEATS',
                    style: TextStyle(color: Color(0xFF10B981), fontSize: 11, fontWeight: FontWeight.bold, letterSpacing: 1.1),
                  ),
                  const SizedBox(height: 14),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Row(
                        children: [
                          IconButton(
                            onPressed: _selectedSeats > 1
                                ? () => setState(() => _selectedSeats--)
                                : null,
                            icon: const Icon(Icons.remove_circle_outline, color: Color(0xFF10B981), size: 28),
                          ),
                          const SizedBox(width: 12),
                          Text(
                            '$_selectedSeats',
                            style: const TextStyle(color: Colors.white, fontSize: 24, fontWeight: FontWeight.bold),
                          ),
                          const SizedBox(width: 12),
                          IconButton(
                            onPressed: _selectedSeats < _maxAvailableSeats
                                ? () => setState(() => _selectedSeats++)
                                : null,
                            icon: const Icon(Icons.add_circle_outline, color: Color(0xFF10B981), size: 28),
                          ),
                        ],
                      ),
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.end,
                        children: [
                          Text('₹${_farePerSeat.toStringAsFixed(0)} / seat', style: const TextStyle(color: Colors.grey, fontSize: 12)),
                          Text(
                            '₹${_totalFare.toStringAsFixed(0)} Total',
                            style: const TextStyle(color: Color(0xFF10B981), fontWeight: FontWeight.bold, fontSize: 20),
                          ),
                        ],
                      ),
                    ],
                  ),
                ],
              ),
            ),
            const SizedBox(height: 20),

            // Payment Method Selector
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
                    'PAYMENT METHOD',
                    style: TextStyle(color: Color(0xFF10B981), fontSize: 11, fontWeight: FontWeight.bold, letterSpacing: 1.1),
                  ),
                  const SizedBox(height: 12),
                  RadioListTile<String>(
                    value: 'UPI_INTENT',
                    groupValue: _paymentMethod,
                    onChanged: (val) => setState(() => _paymentMethod = val!),
                    activeColor: const Color(0xFF10B981),
                    title: const Text('UPI (GPay / PhonePe / Paytm)', style: TextStyle(color: Colors.white, fontSize: 14)),
                    secondary: const Icon(Icons.account_balance_wallet_outlined, color: Color(0xFF10B981)),
                  ),
                  RadioListTile<String>(
                    value: 'WALLET',
                    groupValue: _paymentMethod,
                    onChanged: (val) => setState(() => _paymentMethod = val!),
                    activeColor: const Color(0xFF10B981),
                    title: const Text('RoutBuddy Commute Wallet', style: TextStyle(color: Colors.white, fontSize: 14)),
                    secondary: const Icon(Icons.wallet, color: Colors.blueAccent),
                  ),
                  RadioListTile<String>(
                    value: 'CASH',
                    groupValue: _paymentMethod,
                    onChanged: (val) => setState(() => _paymentMethod = val!),
                    activeColor: const Color(0xFF10B981),
                    title: const Text('Pay Cash to Driver upon Boarding', style: TextStyle(color: Colors.white, fontSize: 14)),
                    secondary: const Icon(Icons.money, color: Colors.amber),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 20),

            if (_errorMessage != null)
              Container(
                margin: const EdgeInsets.only(bottom: 16),
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: Colors.redAccent.withOpacity(0.15),
                  borderRadius: BorderRadius.circular(10),
                ),
                child: Text(_errorMessage!, style: const TextStyle(color: Colors.redAccent, fontSize: 13)),
              ),

            // Confirm Booking Button
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: _isBooking ? null : _handleConfirmBooking,
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF10B981),
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(vertical: 16),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                ),
                child: _isBooking
                    ? const SizedBox(
                        height: 20,
                        width: 20,
                        child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                      )
                    : Text(
                        'Reserve $_selectedSeats Seat(s) • ₹${_totalFare.toStringAsFixed(0)}',
                        style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                      ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
