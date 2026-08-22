import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import '../../../core/constants/api_constants.dart';
import '../../../core/network/api_client.dart';

class CreateTripScreen extends StatefulWidget {
  const CreateTripScreen({Key? key}) : super(key: key);

  @override
  State<CreateTripScreen> createState() => _CreateTripScreenState();
}

class _CreateTripScreenState extends State<CreateTripScreen> {
  List<dynamic> _routes = [];
  List<dynamic> _vehicles = [];
  dynamic _selectedRoute;
  dynamic _selectedVehicle;

  int _totalSeats = 3;
  double _farePerSeat = 20.0;
  bool _isLoading = true;
  bool _isSubmitting = false;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _fetchRoutesAndVehicles();
  }

  Future<void> _fetchRoutesAndVehicles() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final routesRes = await ApiClient().dio.get(ApiConstants.routes);
      final vehiclesRes = await ApiClient().dio.get(ApiConstants.vehicleMy);

      if (routesRes.statusCode == 200 && routesRes.data['data'] != null) {
        _routes = routesRes.data['data'];
        if (_routes.isNotEmpty) _selectedRoute = _routes.first;
      }

      if (vehiclesRes.statusCode == 200 && vehiclesRes.data['data'] != null) {
        _vehicles = vehiclesRes.data['data'];
        if (_vehicles.isNotEmpty) {
          _selectedVehicle = _vehicles.first;
          _totalSeats = (_selectedVehicle['seatingCapacity'] as num?)?.toInt() ?? 3;
        }
      }
    } catch (e) {
      setState(() => _errorMessage = 'Failed to load routes or registered vehicles.');
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  Future<void> _handleCreateAndPublishTrip() async {
    if (_selectedRoute == null || _selectedVehicle == null) {
      setState(() => _errorMessage = 'Please select a route and a vehicle');
      return;
    }

    setState(() {
      _isSubmitting = true;
      _errorMessage = null;
    });

    try {
      // 1. Create Trip
      final tripRes = await ApiClient().dio.post(
        ApiConstants.trips,
        data: {
          'vehicleId': _selectedVehicle['id'],
          'routeId': _selectedRoute['id'],
          'scheduledDeparture': DateTime.now().toUtc().toIso8601String(),
          'totalSeats': _totalSeats,
          'farePerSeatInr': _farePerSeat,
        },
      );

      if (tripRes.statusCode == 201 && tripRes.data['data'] != null) {
        final tripId = tripRes.data['data']['id'];

        // 2. Publish Trip for immediate passenger booking
        await ApiClient().dio.put('${ApiConstants.trips}/$tripId/publish');

        if (!mounted) return;
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Trip published live! Passengers can now book seats.')),
        );
        Navigator.of(context).pop(true);
      }
    } on DioException catch (e) {
      String msg = 'Failed to create trip.';
      if (e.response?.data != null && e.response?.data['message'] != null) {
        msg = e.response!.data['message'];
      }
      setState(() => _errorMessage = msg);
    } catch (e) {
      setState(() => _errorMessage = 'An unexpected error occurred.');
    } finally {
      if (mounted) setState(() => _isSubmitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0B0F19),
      appBar: AppBar(
        backgroundColor: const Color(0xFF111827),
        elevation: 0,
        title: const Text('Dispatch New Trip', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18, color: Colors.white)),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back, color: Colors.white),
          onPressed: () => Navigator.of(context).pop(),
        ),
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator(color: Color(0xFF10B981)))
          : SingleChildScrollView(
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

                  // Route Selection
                  const Text('Select Corridor Route', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 15)),
                  const SizedBox(height: 8),
                  if (_routes.isEmpty)
                    const Text('No routes found. Please create a route first.', style: TextStyle(color: Colors.grey))
                  else
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
                          value: _selectedRoute?['id'],
                          items: _routes.map<DropdownMenuItem<String>>((r) {
                            return DropdownMenuItem<String>(
                              value: r['id'],
                              child: Text(r['name'] ?? 'Route', style: const TextStyle(color: Colors.white)),
                            );
                          }).toList(),
                          onChanged: (val) {
                            if (val != null) {
                              setState(() => _selectedRoute = _routes.firstWhere((r) => r['id'] == val));
                            }
                          },
                        ),
                      ),
                    ),
                  const SizedBox(height: 20),

                  // Vehicle Selection
                  const Text('Select Vehicle', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 15)),
                  const SizedBox(height: 8),
                  if (_vehicles.isEmpty)
                    const Text('No registered vehicles. Please register your vehicle first.', style: TextStyle(color: Colors.grey))
                  else
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
                          value: _selectedVehicle?['id'],
                          items: _vehicles.map<DropdownMenuItem<String>>((v) {
                            return DropdownMenuItem<String>(
                              value: v['id'],
                              child: Text('${v['plateNumber']} (${v['modelName'] ?? 'Vehicle'})', style: const TextStyle(color: Colors.white)),
                            );
                          }).toList(),
                          onChanged: (val) {
                            if (val != null) {
                              setState(() {
                                _selectedVehicle = _vehicles.firstWhere((v) => v['id'] == val);
                                _totalSeats = (_selectedVehicle['seatingCapacity'] as num?)?.toInt() ?? 3;
                              });
                            }
                          },
                        ),
                      ),
                    ),
                  const SizedBox(height: 20),

                  // Capacity & Fare Controls
                  Container(
                    padding: const EdgeInsets.all(16),
                    decoration: BoxDecoration(
                      color: const Color(0xFF182234),
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(color: const Color(0xFF1F2937)),
                    ),
                    child: Column(
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            const Text('Total Seats to Offer:', style: TextStyle(color: Colors.white, fontSize: 14)),
                            Row(
                              children: [
                                IconButton(
                                  onPressed: _totalSeats > 1 ? () => setState(() => _totalSeats--) : null,
                                  icon: const Icon(Icons.remove_circle_outline, color: Color(0xFF10B981)),
                                ),
                                Text('$_totalSeats', style: const TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold)),
                                IconButton(
                                  onPressed: _selectedVehicle != null && _totalSeats < (_selectedVehicle['seatingCapacity'] as num)
                                      ? () => setState(() => _totalSeats++)
                                      : null,
                                  icon: const Icon(Icons.add_circle_outline, color: Color(0xFF10B981)),
                                ),
                              ],
                            ),
                          ],
                        ),
                        const Divider(color: Color(0xFF1F2937), height: 20),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            const Text('Base Fare per Seat (₹):', style: TextStyle(color: Colors.white, fontSize: 14)),
                            SizedBox(
                              width: 80,
                              child: TextField(
                                keyboardType: TextInputType.number,
                                style: const TextStyle(color: Color(0xFF10B981), fontWeight: FontWeight.bold, fontSize: 16),
                                textAlign: TextAlign.right,
                                decoration: const InputDecoration(
                                  border: InputBorder.none,
                                  prefixText: '₹',
                                  prefixStyle: TextStyle(color: Color(0xFF10B981)),
                                ),
                                controller: TextEditingController(text: '$_farePerSeat'),
                                onChanged: (val) {
                                  final num = double.tryParse(val);
                                  if (num != null) _farePerSeat = num;
                                },
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 28),

                  ElevatedButton(
                    onPressed: _isSubmitting || _routes.isEmpty || _vehicles.isEmpty
                        ? null
                        : _handleCreateAndPublishTrip,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color(0xFF10B981),
                      foregroundColor: Colors.white,
                      padding: const EdgeInsets.symmetric(vertical: 16),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                    ),
                    child: _isSubmitting
                        ? const SizedBox(
                            height: 20,
                            width: 20,
                            child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                          )
                        : const Text('Publish Trip & Open Bookings', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                  ),
                ],
              ),
            ),
    );
  }
}
