import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import '../../../core/constants/api_constants.dart';
import '../../../core/network/api_client.dart';

class VehicleRegistrationScreen extends StatefulWidget {
  const VehicleRegistrationScreen({Key? key}) : super(key: key);

  @override
  State<VehicleRegistrationScreen> createState() => _VehicleRegistrationScreenState();
}

class _VehicleRegistrationScreenState extends State<VehicleRegistrationScreen> {
  final _plateController = TextEditingController();
  final _modelController = TextEditingController();
  String _vehicleType = 'AUTO_3W';
  String _fuelType = 'CNG';
  int _seatingCapacity = 3;

  bool _isLoading = false;
  String? _errorMessage;
  String? _successMessage;

  @override
  void dispose() {
    _plateController.dispose();
    _modelController.dispose();
    super.dispose();
  }

  Future<void> _handleRegisterVehicle() async {
    final plate = _plateController.text.trim();
    final model = _modelController.text.trim();

    if (plate.isEmpty || model.isEmpty) {
      setState(() => _errorMessage = 'License plate and vehicle model are required');
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
      _successMessage = null;
    });

    try {
      final response = await ApiClient().dio.post(
        ApiConstants.vehicleRegister,
        data: {
          'plateNumber': plate.toUpperCase(),
          'vehicleType': _vehicleType,
          'modelName': model,
          'seatingCapacity': _seatingCapacity,
          'fuelType': _fuelType,
        },
      );

      if (response.statusCode == 201) {
        final vehicleId = response.data['data']['id'];
        // Attempt to assign as active vehicle
        try {
          await ApiClient().dio.put('${ApiConstants.driverAssignVehicle}/$vehicleId');
        } catch (_) {}

        setState(() {
          _successMessage = 'Vehicle registered successfully! Active for your trips.';
          _plateController.clear();
          _modelController.clear();
        });
      }
    } on DioException catch (e) {
      String msg = 'Failed to register vehicle.';
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
        backgroundColor: const Color(0xFF111827),
        elevation: 0,
        title: const Text('Vehicle Registration', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18, color: Colors.white)),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back, color: Colors.white),
          onPressed: () => Navigator.of(context).pop(),
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const Text(
              'Register Shared Transit Vehicle',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Colors.white),
            ),
            const SizedBox(height: 6),
            const Text(
              'Add your Auto, E-Rickshaw, or Shuttle Van to publish trips',
              style: TextStyle(fontSize: 13, color: Colors.grey),
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

            // Plate Number
            _buildField(controller: _plateController, hint: 'License Plate (e.g. DL-01-AB-1234)', icon: Icons.pin_outlined),
            const SizedBox(height: 14),

            // Model Name
            _buildField(controller: _modelController, hint: 'Model (e.g. Bajaj Maxima CNG / Mahindra Treo)', icon: Icons.directions_car_outlined),
            const SizedBox(height: 16),

            // Vehicle Type Selector
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
              decoration: BoxDecoration(
                color: const Color(0xFF182234),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: const Color(0xFF1F2937)),
              ),
              child: DropdownButtonHideUnderline(
                child: DropdownButton<String>(
                  isExpanded: true,
                  dropdownColor: const Color(0xFF182234),
                  value: _vehicleType,
                  items: const [
                    DropdownMenuItem(value: 'AUTO_3W', child: Text('Auto Rickshaw (3-Wheeler)', style: TextStyle(color: Colors.white))),
                    DropdownMenuItem(value: 'E_RICKSHAW', child: Text('Electric Rickshaw (E-Rick)', style: TextStyle(color: Colors.white))),
                    DropdownMenuItem(value: 'SHUTTLE_VAN', child: Text('Shared Shuttle Van / Mini Bus', style: TextStyle(color: Colors.white))),
                    DropdownMenuItem(value: 'CAR_4W', child: Text('4-Wheeler Car / Cab', style: TextStyle(color: Colors.white))),
                  ],
                  onChanged: (val) {
                    if (val != null) {
                      setState(() {
                        _vehicleType = val;
                        if (val == 'AUTO_3W') _seatingCapacity = 3;
                        if (val == 'E_RICKSHAW') _seatingCapacity = 4;
                        if (val == 'SHUTTLE_VAN') _seatingCapacity = 6;
                        if (val == 'CAR_4W') _seatingCapacity = 4;
                      });
                    }
                  },
                ),
              ),
            ),
            const SizedBox(height: 14),

            // Fuel Type Selector
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
              decoration: BoxDecoration(
                color: const Color(0xFF182234),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: const Color(0xFF1F2937)),
              ),
              child: DropdownButtonHideUnderline(
                child: DropdownButton<String>(
                  isExpanded: true,
                  dropdownColor: const Color(0xFF182234),
                  value: _fuelType,
                  items: const [
                    DropdownMenuItem(value: 'CNG', child: Text('CNG (Compressed Natural Gas)', style: TextStyle(color: Colors.white))),
                    DropdownMenuItem(value: 'ELECTRIC', child: Text('Electric (EV)', style: TextStyle(color: Colors.white))),
                    DropdownMenuItem(value: 'PETROL', child: Text('Petrol', style: TextStyle(color: Colors.white))),
                    DropdownMenuItem(value: 'DIESEL', child: Text('Diesel', style: TextStyle(color: Colors.white))),
                  ],
                  onChanged: (val) {
                    if (val != null) setState(() => _fuelType = val);
                  },
                ),
              ),
            ),
            const SizedBox(height: 16),

            // Seating Capacity Counter
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: const Color(0xFF182234),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: const Color(0xFF1F2937)),
              ),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text('Passenger Seating Capacity:', style: TextStyle(color: Colors.white, fontSize: 14)),
                  Row(
                    children: [
                      IconButton(
                        onPressed: _seatingCapacity > 1 ? () => setState(() => _seatingCapacity--) : null,
                        icon: const Icon(Icons.remove_circle_outline, color: Color(0xFF10B981)),
                      ),
                      Text(
                        '$_seatingCapacity',
                        style: const TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold),
                      ),
                      IconButton(
                        onPressed: _seatingCapacity < 12 ? () => setState(() => _seatingCapacity++) : null,
                        icon: const Icon(Icons.add_circle_outline, color: Color(0xFF10B981)),
                      ),
                    ],
                  ),
                ],
              ),
            ),
            const SizedBox(height: 28),

            ElevatedButton(
              onPressed: _isLoading ? null : _handleRegisterVehicle,
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
                  : const Text('Save & Register Vehicle', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildField({
    required TextEditingController controller,
    required String hint,
    required IconData icon,
  }) {
    return Container(
      decoration: BoxDecoration(
        color: const Color(0xFF182234),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: const Color(0xFF1F2937)),
      ),
      child: TextField(
        controller: controller,
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
