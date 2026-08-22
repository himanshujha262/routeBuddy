import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import '../../../core/constants/api_constants.dart';
import '../../../core/network/api_client.dart';
import '../../../core/storage/token_storage.dart';
import 'trip_details_screen.dart';

class SharedAutoSearchScreen extends StatefulWidget {
  const SharedAutoSearchScreen({Key? key}) : super(key: key);

  @override
  State<SharedAutoSearchScreen> createState() => _SharedAutoSearchScreenState();
}

class _SharedAutoSearchScreenState extends State<SharedAutoSearchScreen> {
  List<dynamic> _routes = [];
  dynamic _selectedRoute;
  dynamic _selectedPickupStop;
  dynamic _selectedDropoffStop;

  List<dynamic> _availableTrips = [];
  bool _isLoadingRoutes = true;
  bool _isSearchingTrips = false;
  String? _errorMessage;
  String _userName = 'Commuter';

  @override
  void initState() {
    super.initState();
    _loadInitialData();
  }

  Future<void> _loadInitialData() async {
    final name = await TokenStorage.getUserName();
    if (name != null && name.isNotEmpty) {
      setState(() => _userName = name);
    }
    await _fetchActiveRoutes();
  }

  Future<void> _fetchActiveRoutes() async {
    setState(() {
      _isLoadingRoutes = true;
      _errorMessage = null;
    });

    try {
      final response = await ApiClient().dio.get(ApiConstants.routes);
      if (response.statusCode == 200 && response.data['data'] != null) {
        final List<dynamic> routes = response.data['data'];
        setState(() {
          _routes = routes;
          if (routes.isNotEmpty) {
            _selectRoute(routes.first);
          }
        });
      }
    } catch (e) {
      setState(() => _errorMessage = 'Failed to load transit routes');
    } finally {
      setState(() => _isLoadingRoutes = false);
    }
  }

  void _selectRoute(dynamic route) {
    setState(() {
      _selectedRoute = route;
      final stops = (route['stops'] as List<dynamic>?) ?? [];
      if (stops.isNotEmpty) {
        _selectedPickupStop = stops.first;
        _selectedDropoffStop = stops.length > 1 ? stops.last : stops.first;
      } else {
        _selectedPickupStop = null;
        _selectedDropoffStop = null;
      }
    });
    _searchTripsForRoute(route['id']);
  }

  Future<void> _searchTripsForRoute(String routeId) async {
    setState(() {
      _isSearchingTrips = true;
      _errorMessage = null;
    });

    try {
      final response = await ApiClient().dio.get(
        ApiConstants.searchTrips,
        queryParameters: {
          'routeId': routeId,
          'minSeats': 1,
        },
      );

      if (response.statusCode == 200 && response.data['data'] != null) {
        setState(() {
          _availableTrips = response.data['data'];
        });
      }
    } catch (e) {
      setState(() => _errorMessage = 'Failed to fetch available trips');
    } finally {
      setState(() => _isSearchingTrips = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final stops = (_selectedRoute != null ? (_selectedRoute['stops'] as List<dynamic>?) : null) ?? [];

    return Scaffold(
      backgroundColor: const Color(0xFF0B0F19),
      appBar: AppBar(
        backgroundColor: const Color(0xFF111827),
        elevation: 0,
        title: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(6),
              decoration: BoxDecoration(
                color: const Color(0xFF10B981),
                borderRadius: BorderRadius.circular(8),
              ),
              child: const Icon(Icons.navigation, color: Colors.white, size: 20),
            ),
            const SizedBox(width: 10),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'RoutBuddy',
                  style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18, color: Colors.white),
                ),
                Text(
                  'Hello, $_userName',
                  style: const TextStyle(fontSize: 11, color: Colors.grey),
                ),
              ],
            ),
          ],
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh, color: Colors.white),
            onPressed: () {
              if (_selectedRoute != null) {
                _searchTripsForRoute(_selectedRoute['id']);
              } else {
                _fetchActiveRoutes();
              }
            },
          ),
        ],
      ),
      body: _isLoadingRoutes
          ? const Center(child: CircularProgressIndicator(color: Color(0xFF10B981)))
          : RefreshIndicator(
              onRefresh: () async {
                if (_selectedRoute != null) {
                  await _searchTripsForRoute(_selectedRoute['id']);
                }
              },
              color: const Color(0xFF10B981),
              child: SingleChildScrollView(
                physics: const AlwaysScrollableScrollPhysics(),
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // Route Selector Dropdown Card
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
                            'SELECT TRANSIT CORRIDOR',
                            style: TextStyle(
                              color: Color(0xFF10B981),
                              fontSize: 10,
                              fontWeight: FontWeight.bold,
                              letterSpacing: 1.1,
                            ),
                          ),
                          const SizedBox(height: 8),
                          if (_routes.isEmpty)
                            const Text('No active routes found', style: TextStyle(color: Colors.white70))
                          else
                            DropdownButtonHideUnderline(
                              child: DropdownButton<String>(
                                isExpanded: true,
                                dropdownColor: const Color(0xFF182234),
                                value: _selectedRoute?['id'],
                                icon: const Icon(Icons.arrow_drop_down, color: Color(0xFF10B981)),
                                items: _routes.map<DropdownMenuItem<String>>((r) {
                                  return DropdownMenuItem<String>(
                                    value: r['id'],
                                    child: Text(
                                      r['name'] ?? 'Route',
                                      style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 14),
                                    ),
                                  );
                                }).toList(),
                                onChanged: (newRouteId) {
                                  if (newRouteId != null) {
                                    final found = _routes.firstWhere((r) => r['id'] == newRouteId);
                                    _selectRoute(found);
                                  }
                                },
                              ),
                            ),
                          const Divider(color: Color(0xFF1F2937), height: 20),

                          // Pickup & Dropoff Stops Selection
                          if (stops.isNotEmpty) ...[
                            Row(
                              children: [
                                const Icon(Icons.my_location, color: Color(0xFF10B981), size: 18),
                                const SizedBox(width: 10),
                                Expanded(
                                  child: DropdownButtonHideUnderline(
                                    child: DropdownButton<String>(
                                      isExpanded: true,
                                      dropdownColor: const Color(0xFF182234),
                                      value: _selectedPickupStop?['id'],
                                      items: stops.map<DropdownMenuItem<String>>((s) {
                                        return DropdownMenuItem<String>(
                                          value: s['id'],
                                          child: Text(
                                            'Pickup: ${s['stopName']}',
                                            style: const TextStyle(color: Colors.white, fontSize: 13),
                                          ),
                                        );
                                      }).toList(),
                                      onChanged: (newStopId) {
                                        if (newStopId != null) {
                                          setState(() {
                                            _selectedPickupStop = stops.firstWhere((s) => s['id'] == newStopId);
                                          });
                                        }
                                      },
                                    ),
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
                                  child: DropdownButtonHideUnderline(
                                    child: DropdownButton<String>(
                                      isExpanded: true,
                                      dropdownColor: const Color(0xFF182234),
                                      value: _selectedDropoffStop?['id'],
                                      items: stops.map<DropdownMenuItem<String>>((s) {
                                        return DropdownMenuItem<String>(
                                          value: s['id'],
                                          child: Text(
                                            'Dropoff: ${s['stopName']}',
                                            style: const TextStyle(color: Colors.white, fontSize: 13),
                                          ),
                                        );
                                      }).toList(),
                                      onChanged: (newStopId) {
                                        if (newStopId != null) {
                                          setState(() {
                                            _selectedDropoffStop = stops.firstWhere((s) => s['id'] == newStopId);
                                          });
                                        }
                                      },
                                    ),
                                  ),
                                ),
                              ],
                            ),
                          ],
                        ],
                      ),
                    ),
                    const SizedBox(height: 20),

                    // Available Shared Autos Header
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text(
                          'Available Trips on Corridor',
                          style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.white),
                        ),
                        if (_isSearchingTrips)
                          const SizedBox(
                            height: 14,
                            width: 14,
                            child: CircularProgressIndicator(strokeWidth: 2, color: Color(0xFF10B981)),
                          )
                        else
                          Text(
                            '${_availableTrips.length} Available',
                            style: const TextStyle(color: Color(0xFF10B981), fontSize: 12, fontWeight: FontWeight.bold),
                          ),
                      ],
                    ),
                    const SizedBox(height: 12),

                    // Error Message
                    if (_errorMessage != null)
                      Container(
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: Colors.redAccent.withOpacity(0.15),
                          borderRadius: BorderRadius.circular(10),
                        ),
                        child: Text(_errorMessage!, style: const TextStyle(color: Colors.redAccent, fontSize: 13)),
                      ),

                    // Trip List
                    if (_availableTrips.isEmpty && !_isSearchingTrips)
                      Container(
                        padding: const EdgeInsets.all(32),
                        alignment: Alignment.center,
                        child: Column(
                          children: const [
                            Icon(Icons.directions_car_outlined, size: 48, color: Colors.grey),
                            SizedBox(height: 12),
                            Text(
                              'No scheduled trips available right now.\nCheck back shortly or pull down to refresh.',
                              textAlign: TextAlign.center,
                              style: TextStyle(color: Colors.grey, fontSize: 13),
                            ),
                          ],
                        ),
                      )
                    else
                      ..._availableTrips.map((trip) => _buildTripCard(trip, context)).toList(),
                  ],
                ),
              ),
            ),
    );
  }

  Widget _buildTripCard(dynamic trip, BuildContext context) {
    final availableSeats = trip['availableSeats'] ?? 0;
    final totalSeats = trip['totalSeats'] ?? 0;
    final fare = trip['farePerSeatInr'] ?? 0.0;
    final plate = trip['vehiclePlateNumber'] ?? 'Auto';
    final vehicleType = trip['vehicleType'] ?? 'AUTO_3W';
    final status = trip['status'] ?? 'PUBLISHED';

    return Container(
      margin: const EdgeInsets.only(bottom: 14),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: const Color(0xFF182234),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: const Color(0xFF1F2937)),
      ),
      child: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  CircleAvatar(
                    backgroundColor: const Color(0xFF10B981).withOpacity(0.2),
                    child: const Icon(Icons.electric_rickshaw, color: Color(0xFF10B981)),
                  ),
                  const SizedBox(width: 12),
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        plate,
                        style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15, color: Colors.white),
                      ),
                      Text(
                        '$vehicleType • Shared Ride',
                        style: const TextStyle(color: Colors.grey, fontSize: 12),
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
                  status,
                  style: const TextStyle(color: Color(0xFF10B981), fontWeight: FontWeight.bold, fontSize: 11),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),

          // Seat and Status indicator
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: const Color(0xFF111827),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    const Icon(Icons.airline_seat_recline_normal, color: Color(0xFF10B981), size: 16),
                    const SizedBox(width: 6),
                    Text(
                      '$availableSeats of $totalSeats Seats Available',
                      style: const TextStyle(color: Color(0xFF10B981), fontWeight: FontWeight.bold, fontSize: 12),
                    ),
                  ],
                ),
                Text(
                  '₹${fare.toStringAsFixed(0)} / seat',
                  style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 14),
                ),
              ],
            ),
          ),
          const SizedBox(height: 14),

          // Book Button
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: availableSeats > 0 && _selectedPickupStop != null && _selectedDropoffStop != null
                  ? () {
                      Navigator.of(context).push(
                        MaterialPageRoute(
                          builder: (_) => TripDetailsScreen(
                            trip: trip,
                            route: _selectedRoute,
                            pickupStop: _selectedPickupStop,
                            dropoffStop: _selectedDropoffStop,
                          ),
                        ),
                      );
                    }
                  : null,
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF10B981),
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(vertical: 12),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
              ),
              child: const Text('Select Seats & Book', style: TextStyle(fontWeight: FontWeight.bold)),
            ),
          ),
        ],
      ),
    );
  }
}
