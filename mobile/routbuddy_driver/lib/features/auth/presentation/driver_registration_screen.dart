import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import '../../../core/constants/api_constants.dart';
import '../../../core/network/api_client.dart';
import '../../../core/storage/token_storage.dart';
import '../../../main.dart';

class DriverRegistrationScreen extends StatefulWidget {
  const DriverRegistrationScreen({Key? key}) : super(key: key);

  @override
  State<DriverRegistrationScreen> createState() => _DriverRegistrationScreenState();
}

class _DriverRegistrationScreenState extends State<DriverRegistrationScreen> {
  final _nameController = TextEditingController();
  final _phoneController = TextEditingController();
  final _passwordController = TextEditingController();
  final _licenseController = TextEditingController();
  final _aadhaarController = TextEditingController();

  bool _isLoading = false;
  String? _errorMessage;

  @override
  void dispose() {
    _nameController.dispose();
    _phoneController.dispose();
    _passwordController.dispose();
    _licenseController.dispose();
    _aadhaarController.dispose();
    super.dispose();
  }

  Future<void> _handleRegisterDriver() async {
    final name = _nameController.text.trim();
    final phone = _phoneController.text.trim();
    final password = _passwordController.text.trim();
    final license = _licenseController.text.trim();
    final aadhaar = _aadhaarController.text.trim();

    if (name.length < 2) {
      setState(() => _errorMessage = 'Full name must be at least 2 characters long');
      return;
    }

    final phoneRegex = RegExp(r'^[6-9]\d{9}$');
    if (!phoneRegex.hasMatch(phone)) {
      setState(() => _errorMessage = 'Please enter a valid 10-digit mobile number starting with 6-9 (no +91 or spaces)');
      return;
    }

    if (password.length < 6) {
      setState(() => _errorMessage = 'Password must be at least 6 characters long');
      return;
    }

    if (license.isEmpty) {
      setState(() => _errorMessage = 'Driving License number is required');
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      // 1. Register User Account with DRIVER role
      final authRes = await ApiClient().dio.post(
        ApiConstants.register,
        data: {
          'phone': phone,
          'fullName': name,
          'password': password,
          'role': 'DRIVER',
        },
      );

      if (authRes.statusCode == 201 && authRes.data['data'] != null) {
        final authData = authRes.data['data'];
        final user = authData['user'] ?? authData;

        await TokenStorage.saveSession(
          accessToken: authData['accessToken']?.toString() ?? '',
          refreshToken: authData['refreshToken']?.toString() ?? '',
          userId: (user['userId'] ?? user['id'])?.toString() ?? '',
          fullName: user['fullName']?.toString() ?? '',
          phone: user['phone']?.toString() ?? '',
        );

        // 2. Submit Driver Profile with License
        final driverRes = await ApiClient().dio.post(
          ApiConstants.driverRegister,
          data: {
            'licenseNumber': license,
            'licenseExpiryDate': '2030-12-31',
            'aadhaarMasked': aadhaar.isNotEmpty ? aadhaar : 'XXXX-XXXX-9999',
          },
        );

        if (driverRes.statusCode == 201 && driverRes.data['data'] != null) {
          await TokenStorage.saveDriverProfileId(driverRes.data['data']['id']);
        }

        if (!mounted) return;
        Navigator.of(context).pushAndRemoveUntil(
          MaterialPageRoute(builder: (_) => const DriverMainNavigationShell()),
          (route) => false,
        );
      }
    } on DioException catch (e) {
      String msg = 'Registration failed. Please check your inputs.';
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
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back, color: Colors.white),
          onPressed: () => Navigator.of(context).pop(),
        ),
      ),
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(24.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Row(
                  children: [
                    Container(
                      decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(16),
                        boxShadow: [
                          BoxShadow(
                            color: const Color(0xFF10B981).withOpacity(0.25),
                            blurRadius: 12,
                          ),
                        ],
                      ),
                      child: ClipRRect(
                        borderRadius: BorderRadius.circular(16),
                        child: Image.asset(
                          'assets/images/app_logo.png',
                          width: 52,
                          height: 52,
                          fit: BoxFit.cover,
                          errorBuilder: (_, __, ___) => const Icon(Icons.electric_rickshaw, color: Color(0xFF10B981), size: 36),
                        ),
                      ),
                    ),
                    const SizedBox(width: 14),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: const [
                          Text(
                            'Driver Partner',
                            style: TextStyle(
                              fontSize: 22,
                              fontWeight: FontWeight.bold,
                              color: Colors.white,
                            ),
                          ),
                          SizedBox(height: 4),
                          Text(
                            'Register auto & start earning',
                            style: TextStyle(fontSize: 12, color: Colors.grey),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 24),

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

                _buildField(controller: _nameController, hint: 'Full Name', icon: Icons.person_outline),
                const SizedBox(height: 14),
                _buildField(controller: _phoneController, hint: '10-digit Phone Number', icon: Icons.phone_outlined, keyboardType: TextInputType.phone),
                const SizedBox(height: 14),
                _buildField(controller: _passwordController, hint: 'Password (min 6 chars)', icon: Icons.lock_outline, obscureText: true),
                const SizedBox(height: 14),
                _buildField(controller: _licenseController, hint: 'Driving License No. (e.g. DL-0420110012345)', icon: Icons.badge_outlined),
                const SizedBox(height: 14),
                _buildField(controller: _aadhaarController, hint: 'Aadhaar Masked (e.g. XXXX-XXXX-1234)', icon: Icons.credit_card_outlined),
                const SizedBox(height: 28),

                ElevatedButton(
                  onPressed: _isLoading ? null : _handleRegisterDriver,
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
                      : const Text('Submit Onboarding Application', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildField({
    required TextEditingController controller,
    required String hint,
    required IconData icon,
    TextInputType keyboardType = TextInputType.text,
    bool obscureText = false,
  }) {
    return Container(
      decoration: BoxDecoration(
        color: const Color(0xFF182234),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: const Color(0xFF1F2937)),
      ),
      child: TextField(
        controller: controller,
        keyboardType: keyboardType,
        obscureText: obscureText,
        style: const TextStyle(color: Colors.white),
        decoration: InputDecoration(
          hintText: hint,
          hintStyle: const TextStyle(color: Colors.grey, fontSize: 13),
          prefixIcon: Icon(icon, color: const Color(0xFF10B981)),
          border: InputBorder.none,
          contentPadding: const EdgeInsets.symmetric(vertical: 16),
        ),
      ),
    );
  }
}
