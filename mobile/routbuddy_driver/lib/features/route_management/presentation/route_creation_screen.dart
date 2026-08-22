import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import '../../../core/constants/api_constants.dart';
import '../../../core/network/api_client.dart';

class RouteCreationScreen extends StatefulWidget {
  const RouteCreationScreen({Key? key}) : super(key: key);

  @override
  State<RouteCreationScreen> createState() => _RouteCreationScreenState();
}

class _RouteCreationScreenState extends State<RouteCreationScreen> {
  final _routeNameController = TextEditingController();
  final _originNameController = TextEditingController();
  final _destinationNameController = TextEditingController();
  final _baseFareController = TextEditingController(text: '20.0');
  final _distanceKmController = TextEditingController(text: '8.5');
  final _durationMinController = TextEditingController(text: '25');

  final List<Map<String, dynamic>> _stops = [
    {'stopName': 'Origin Stand', 'sequenceOrder': 1, 'distanceFromOriginKm': 0.0, 'stageFareInr': 10.0, 'latitude': 28.5645, 'longitude': 77.3345},
    {'stopName': 'Midway Junction', 'sequenceOrder': 2, 'distanceFromOriginKm': 4.0, 'stageFareInr': 15.0, 'latitude': 28.5900, 'longitude': 77.3500},
    {'stopName': 'Destination Terminal', 'sequenceOrder': 3, 'distanceFromOriginKm': 8.5, 'stageFareInr': 20.0, 'latitude': 28.6280, 'longitude': 77.3730},
  ];

  bool _isLoading = false;
  String? _errorMessage;
  String? _successMessage;

  @override
  void dispose() {
    _routeNameController.dispose();
    _originNameController.dispose();
    _destinationNameController.dispose();
    _baseFareController.dispose();
    _distanceKmController.dispose();
    _durationMinController.dispose();
    super.dispose();
  }

  void _addStop() {
    setState(() {
      final nextOrder = _stops.length + 1;
      _stops.add({
        'stopName': 'Stop $nextOrder',
        'sequenceOrder': nextOrder,
        'distanceFromOriginKm': (_stops.last['distanceFromOriginKm'] as double) + 2.0,
        'stageFareInr': (_stops.last['stageFareInr'] as double) + 5.0,
        'latitude': 28.6000,
        'longitude': 77.3600,
      });
    });
  }

  Future<void> _handleCreateRoute() async {
    final routeName = _routeNameController.text.trim();
    final origin = _originNameController.text.trim();
    final destination = _destinationNameController.text.trim();

    if (routeName.isEmpty || origin.isEmpty || destination.isEmpty) {
      setState(() => _errorMessage = 'Route name, origin, and destination are required');
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
      _successMessage = null;
    });

    try {
      final baseFare = double.tryParse(_baseFareController.text) ?? 20.0;
      final distance = double.tryParse(_distanceKmController.text) ?? 8.5;
      final duration = int.tryParse(_durationMinController.text) ?? 25;

      final response = await ApiClient().dio.post(
        ApiConstants.routes,
        data: {
          'name': routeName,
          'originName': origin,
          'destinationName': destination,
          'originLatitude': _stops.first['latitude'],
          'originLongitude': _stops.first['longitude'],
          'destinationLatitude': _stops.last['latitude'],
          'destinationLongitude': _stops.last['longitude'],
          'baseFareInr': baseFare,
          'totalDistanceKm': distance,
          'estimatedDurationMin': duration,
          'stops': _stops.map((s) => {
            'stopName': s['stopName'],
            'sequenceOrder': s['sequenceOrder'],
            'latitude': s['latitude'],
            'longitude': s['longitude'],
            'distanceFromOriginKm': s['distanceFromOriginKm'],
            'stageFareInr': s['stageFareInr'],
            'radiusMeters': 50,
          }).toList(),
        },
      );

      if (response.statusCode == 201) {
        setState(() {
          _successMessage = 'Transit route created successfully with ${_stops.length} stops!';
          _routeNameController.clear();
          _originNameController.clear();
          _destinationNameController.clear();
        });
      }
    } on DioException catch (e) {
      String msg = 'Failed to create transit route.';
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
        title: const Text('Create Corridor Route', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18, color: Colors.white)),
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

            _buildField(controller: _routeNameController, hint: 'Route Name (e.g. Botanical Garden to Sec 62 Corridor)', icon: Icons.alt_route),
            const SizedBox(height: 12),
            _buildField(controller: _originNameController, hint: 'Origin Stand (e.g. Botanical Garden Metro)', icon: Icons.my_location),
            const SizedBox(height: 12),
            _buildField(controller: _destinationNameController, hint: 'Destination Terminal (e.g. Noida Sector 62)', icon: Icons.location_on),
            const SizedBox(height: 12),

            Row(
              children: [
                Expanded(child: _buildField(controller: _baseFareController, hint: 'Base Fare ₹', icon: Icons.currency_rupee, keyboardType: TextInputType.number)),
                const SizedBox(width: 10),
                Expanded(child: _buildField(controller: _distanceKmController, hint: 'Distance Km', icon: Icons.straighten, keyboardType: TextInputType.number)),
                const SizedBox(width: 10),
                Expanded(child: _buildField(controller: _durationMinController, hint: 'Duration Min', icon: Icons.timer, keyboardType: TextInputType.number)),
              ],
            ),
            const SizedBox(height: 24),

            // Stops Section
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text('Sequential Route Stops', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 16)),
                TextButton.icon(
                  onPressed: _addStop,
                  icon: const Icon(Icons.add, color: Color(0xFF10B981), size: 18),
                  label: const Text('Add Stop', style: TextStyle(color: Color(0xFF10B981))),
                ),
              ],
            ),
            const SizedBox(height: 8),

            ..._stops.asMap().entries.map((entry) {
              final idx = entry.key;
              final stop = entry.value;
              return Container(
                margin: const EdgeInsets.only(bottom: 10),
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: const Color(0xFF182234),
                  borderRadius: BorderRadius.circular(10),
                  border: Border.all(color: const Color(0xFF1F2937)),
                ),
                child: Row(
                  children: [
                    CircleAvatar(
                      radius: 12,
                      backgroundColor: const Color(0xFF10B981).withOpacity(0.2),
                      child: Text('${idx + 1}', style: const TextStyle(color: Color(0xFF10B981), fontSize: 11, fontWeight: FontWeight.bold)),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(stop['stopName'], style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 13)),
                          Text('Km ${stop['distanceFromOriginKm']} • Stage Fare: ₹${stop['stageFareInr']}', style: const TextStyle(color: Colors.grey, fontSize: 11)),
                        ],
                      ),
                    ),
                  ],
                ),
              );
            }).toList(),
            const SizedBox(height: 24),

            ElevatedButton(
              onPressed: _isLoading ? null : _handleCreateRoute,
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
                  : const Text('Publish Transit Route', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
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
    TextInputType keyboardType = TextInputType.text,
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
