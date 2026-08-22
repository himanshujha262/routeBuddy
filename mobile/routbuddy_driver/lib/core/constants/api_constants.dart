class ApiConstants {
  // Configurable base URL: http://localhost:8080 with adb reverse tcp:8080 tcp:8080 over USB
  static const String baseUrl = 'http://localhost:8080';

  // Auth endpoints
  static const String register = '/api/v1/auth/register';
  static const String login = '/api/v1/auth/login';
  static const String refreshToken = '/api/v1/auth/refresh-token';

  // Driver endpoints
  static const String driverRegister = '/api/v1/drivers/register';
  static const String driverMe = '/api/v1/drivers/me';
  static const String driverStatus = '/api/v1/drivers/me/status';
  static const String driverAssignVehicle = '/api/v1/drivers/me/vehicle';

  // Vehicle endpoints
  static const String vehicleRegister = '/api/v1/vehicles/register';
  static const String vehicleMy = '/api/v1/vehicles/my-vehicles';

  // Route endpoints
  static const String routes = '/api/v1/routes';

  // Trip endpoints
  static const String trips = '/api/v1/trips';
  static const String driverActiveTrip = '/api/v1/trips/driver/active';

  // Booking verification endpoint
  static const String verifyBoarding = '/api/v1/bookings/verify-boarding';
}
