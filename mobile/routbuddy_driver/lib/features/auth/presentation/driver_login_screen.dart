import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import '../../../core/constants/api_constants.dart';
import '../../../core/network/api_client.dart';
import '../../../core/storage/token_storage.dart';
import '../../../main.dart';
import 'driver_registration_screen.dart';

class DriverLoginScreen extends StatefulWidget {
  const DriverLoginScreen({Key? key}) : super(key: key);

  @override
  State<DriverLoginScreen> createState() => _DriverLoginScreenState();
}

class _DriverLoginScreenState extends State<DriverLoginScreen> {
  final _phoneController = TextEditingController();
  final _passwordController = TextEditingController();
  bool _isLoading = false;
  String? _errorMessage;

  @override
  void dispose() {
    _phoneController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  Future<void> _handleLogin() async {
    final phone = _phoneController.text.trim();
    final password = _passwordController.text.trim();

    if (phone.isEmpty || password.isEmpty) {
      setState(() => _errorMessage = 'Please enter phone number and password');
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final response = await ApiClient().dio.post(
        ApiConstants.login,
        data: {
          'identifier': phone,
          'password': password,
        },
      );

      if (response.statusCode == 200 && response.data['data'] != null) {
        final data = response.data['data'];
        final user = data['user'] ?? data;

        await TokenStorage.saveSession(
          accessToken: data['accessToken']?.toString() ?? '',
          refreshToken: data['refreshToken']?.toString() ?? '',
          userId: (user['userId'] ?? user['id'])?.toString() ?? '',
          fullName: user['fullName']?.toString() ?? '',
          phone: user['phone']?.toString() ?? '',
        );

        // Fetch driver profile to check if registered
        try {
          final driverRes = await ApiClient().dio.get(ApiConstants.driverMe);
          if (driverRes.statusCode == 200 && driverRes.data['data'] != null) {
            await TokenStorage.saveDriverProfileId(driverRes.data['data']['id']);
          }
        } catch (_) {
          // Driver profile might not exist yet
        }

        if (!mounted) return;
        Navigator.of(context).pushReplacement(
          MaterialPageRoute(builder: (_) => const DriverMainNavigationShell()),
        );
      }
    } on DioException catch (e) {
      String msg = 'Login failed. Invalid credentials or network error.';
      if (e.response?.data != null && e.response?.data['message'] != null) {
        msg = e.response!.data['message'];
      }
      setState(() => _errorMessage = msg);
    } catch (e) {
      setState(() => _errorMessage = 'An unexpected error occurred.');
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0B0F19),
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(24.0),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Center(
                  child: Container(
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(24),
                      boxShadow: [
                        BoxShadow(
                          color: const Color(0xFF10B981).withOpacity(0.3),
                          blurRadius: 20,
                          spreadRadius: 2,
                        ),
                      ],
                    ),
                    child: ClipRRect(
                      borderRadius: BorderRadius.circular(24),
                      child: Image.asset(
                        'assets/images/app_logo.png',
                        width: 96,
                        height: 96,
                        fit: BoxFit.cover,
                        errorBuilder: (_, __, ___) => Container(
                          width: 96,
                          height: 96,
                          color: const Color(0xFF10B981).withOpacity(0.15),
                          child: const Icon(Icons.electric_rickshaw, size: 48, color: Color(0xFF10B981)),
                        ),
                      ),
                    ),
                  ),
                ),
                const SizedBox(height: 20),
                const Text(
                  'RoutBuddy Driver',
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontSize: 26,
                    fontWeight: FontWeight.bold,
                    color: Colors.white,
                    letterSpacing: 1.2,
                  ),
                ),
                const SizedBox(height: 6),
                const Text(
                  'Driver Partner Portal & Dispatching',
                  textAlign: TextAlign.center,
                  style: TextStyle(fontSize: 13, color: Colors.grey),
                ),
                const SizedBox(height: 36),

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

                // Phone Input
                Container(
                  decoration: BoxDecoration(
                    color: const Color(0xFF182234),
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(color: const Color(0xFF1F2937)),
                  ),
                  child: TextField(
                    controller: _phoneController,
                    keyboardType: TextInputType.phone,
                    style: const TextStyle(color: Colors.white),
                    decoration: const InputDecoration(
                      hintText: '10-digit Driver Phone Number',
                      hintStyle: TextStyle(color: Colors.grey, fontSize: 13),
                      prefixIcon: Icon(Icons.phone_outlined, color: Color(0xFF10B981)),
                      border: InputBorder.none,
                      contentPadding: EdgeInsets.symmetric(vertical: 16),
                    ),
                  ),
                ),
                const SizedBox(height: 16),

                // Password Input
                Container(
                  decoration: BoxDecoration(
                    color: const Color(0xFF182234),
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(color: const Color(0xFF1F2937)),
                  ),
                  child: TextField(
                    controller: _passwordController,
                    obscureText: true,
                    style: const TextStyle(color: Colors.white),
                    decoration: const InputDecoration(
                      hintText: 'Password',
                      hintStyle: TextStyle(color: Colors.grey, fontSize: 13),
                      prefixIcon: Icon(Icons.lock_outline, color: Color(0xFF10B981)),
                      border: InputBorder.none,
                      contentPadding: EdgeInsets.symmetric(vertical: 16),
                    ),
                  ),
                ),
                const SizedBox(height: 24),

                ElevatedButton(
                  onPressed: _isLoading ? null : _handleLogin,
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
                      : const Text('Driver Sign In', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                ),
                const SizedBox(height: 20),

                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    const Text("New Driver Partner? ", style: TextStyle(color: Colors.grey, fontSize: 13)),
                    GestureDetector(
                      onTap: () {
                        Navigator.of(context).push(
                          MaterialPageRoute(builder: (_) => const DriverRegistrationScreen()),
                        );
                      },
                      child: const Text(
                        'Onboard Here',
                        style: TextStyle(color: Color(0xFF10B981), fontWeight: FontWeight.bold, fontSize: 13),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
