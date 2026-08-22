import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import '../../../core/constants/api_constants.dart';
import '../../../core/network/api_client.dart';

class DriverQrScannerScreen extends StatefulWidget {
  const DriverQrScannerScreen({Key? key}) : super(key: key);

  @override
  State<DriverQrScannerScreen> createState() => _DriverQrScannerScreenState();
}

class _DriverQrScannerScreenState extends State<DriverQrScannerScreen> {
  final _qrTokenController = TextEditingController();
  final _otpController = TextEditingController();
  dynamic _verifiedBooking;
  bool _isLoading = false;
  String? _errorMessage;
  String? _successMessage;

  @override
  void dispose() {
    _qrTokenController.dispose();
    _otpController.dispose();
    super.dispose();
  }

  Future<void> _handleVerifyBoarding({String? token, String? otp}) async {
    final qrToken = token ?? _qrTokenController.text.trim();
    final otpCode = otp ?? _otpController.text.trim();

    if (qrToken.isEmpty && otpCode.isEmpty) {
      setState(() => _errorMessage = 'Please enter either QR Token or 6-digit OTP');
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
      _successMessage = null;
      _verifiedBooking = null;
    });

    try {
      final response = await ApiClient().dio.post(
        ApiConstants.verifyBoarding,
        data: {
          'qrToken': qrToken.isNotEmpty ? qrToken : null,
          'otpCode': otpCode.isNotEmpty ? otpCode : null,
        },
      );

      if (response.statusCode == 200 && response.data['data'] != null) {
        setState(() {
          _verifiedBooking = response.data['data'];
          _successMessage = 'Passenger boarded successfully! Booking ${_verifiedBooking['bookingCode']} verified.';
          _qrTokenController.clear();
          _otpController.clear();
        });
      }
    } on DioException catch (e) {
      String msg = 'Boarding verification failed.';
      if (e.response?.data != null && e.response?.data['message'] != null) {
        msg = e.response!.data['message'];
      }
      setState(() => _errorMessage = msg);
    } catch (e) {
      setState(() => _errorMessage = 'An unexpected error occurred during verification.');
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0B0F19),
      appBar: AppBar(
        backgroundColor: const Color(0xFF111827),
        elevation: 0,
        title: const Text('Verify Boarding Pass', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18, color: Colors.white)),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Scanner / Verification Card
            Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: const Color(0xFF182234),
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: const Color(0xFF10B981).withOpacity(0.3)),
              ),
              child: Column(
                children: [
                  const Icon(Icons.qr_code_scanner, size: 56, color: Color(0xFF10B981)),
                  const SizedBox(height: 12),
                  const Text(
                    'Passenger Boarding Scanner',
                    style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 18),
                  ),
                  const SizedBox(height: 6),
                  const Text(
                    'Scan passenger dynamic QR pass or enter 6-digit OTP',
                    textAlign: TextAlign.center,
                    style: TextStyle(color: Colors.grey, fontSize: 12),
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
                  border: Border.all(color: Colors.redAccent),
                ),
                child: Text(_errorMessage!, style: const TextStyle(color: Colors.redAccent, fontSize: 13)),
              ),

            if (_successMessage != null)
              Container(
                margin: const EdgeInsets.only(bottom: 16),
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: const Color(0xFF10B981).withOpacity(0.15),
                  borderRadius: BorderRadius.circular(10),
                  border: Border.all(color: const Color(0xFF10B981)),
                ),
                child: Text(_successMessage!, style: const TextStyle(color: Color(0xFF10B981), fontSize: 13, fontWeight: FontWeight.bold)),
              ),

            // OTP Manual Input Section
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: const Color(0xFF182234),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: const Color(0xFF1F2937)),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('OPTION 1: 6-DIGIT OTP CODE', style: TextStyle(color: Color(0xFF10B981), fontSize: 11, fontWeight: FontWeight.bold, letterSpacing: 1.1)),
                  const SizedBox(height: 10),
                  TextField(
                    controller: _otpController,
                    keyboardType: TextInputType.number,
                    style: const TextStyle(color: Colors.white, fontSize: 18, letterSpacing: 3.0),
                    decoration: const InputDecoration(
                      hintText: 'e.g. 849201',
                      hintStyle: TextStyle(color: Colors.grey, fontSize: 14, letterSpacing: 1.0),
                      prefixIcon: Icon(Icons.pin, color: Color(0xFF10B981)),
                      border: InputBorder.none,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 14),

            // QR Token Input Section
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: const Color(0xFF182234),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: const Color(0xFF1F2937)),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('OPTION 2: QR BOARDING TOKEN', style: TextStyle(color: Color(0xFF10B981), fontSize: 11, fontWeight: FontWeight.bold, letterSpacing: 1.1)),
                  const SizedBox(height: 10),
                  TextField(
                    controller: _qrTokenController,
                    style: const TextStyle(color: Colors.white, fontSize: 13),
                    decoration: const InputDecoration(
                      hintText: 'Paste or scan encrypted QR token',
                      hintStyle: TextStyle(color: Colors.grey, fontSize: 13),
                      prefixIcon: Icon(Icons.qr_code, color: Color(0xFF10B981)),
                      border: InputBorder.none,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 24),

            ElevatedButton(
              onPressed: _isLoading ? null : () => _handleVerifyBoarding(),
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF10B981),
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(vertical: 16),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
              ),
              child: _isLoading
                  ? const SizedBox(
                      height: 20,
                      width: 20,
                      child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                    )
                  : const Text('Verify & Board Passenger', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
            ),
            const SizedBox(height: 20),

            // Verified Passenger Details Card
            if (_verifiedBooking != null)
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: const Color(0xFF182234),
                  borderRadius: BorderRadius.circular(14),
                  border: Border.all(color: const Color(0xFF10B981)),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text('BOARDING CONFIRMED', style: TextStyle(color: Color(0xFF10B981), fontSize: 11, fontWeight: FontWeight.bold)),
                        Text('Booking ${_verifiedBooking['bookingCode']}', style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 14)),
                      ],
                    ),
                    const Divider(color: Color(0xFF1F2937), height: 20),
                    Text('Seats: ${_verifiedBooking['seatCount']}', style: const TextStyle(color: Colors.white70, fontSize: 13)),
                    const SizedBox(height: 4),
                    Text('Pickup: ${_verifiedBooking['pickupStopName']}', style: const TextStyle(color: Colors.white70, fontSize: 13)),
                    const SizedBox(height: 4),
                    Text('Dropoff: ${_verifiedBooking['dropoffStopName']}', style: const TextStyle(color: Colors.white70, fontSize: 13)),
                    const SizedBox(height: 4),
                    Text('Fare Collected: ₹${_verifiedBooking['fareAmountInr']}', style: const TextStyle(color: Color(0xFF10B981), fontWeight: FontWeight.bold, fontSize: 14)),
                  ],
                ),
              ),
          ],
        ),
      ),
    );
  }
}
