class ApiConstants {
  // Configurable base URL: http://localhost:8080 with adb reverse tcp:8080 tcp:8080 over USB
  static const String baseUrl = 'http://localhost:8080';

  // Auth endpoints
  static const String register = '/api/v1/auth/register';
  static const String login = '/api/v1/auth/login';
  static const String refreshToken = '/api/v1/auth/refresh-token';

  // Route endpoints
  static const String routes = '/api/v1/routes';

  // Trip endpoints
  static const String trips = '/api/v1/trips';
  static const String searchTrips = '/api/v1/bookings/search';

  // Booking endpoints
  static const String bookings = '/api/v1/bookings';
  static const String myBookings = '/api/v1/bookings/my-bookings';
  static const String cancelBooking = '/api/v1/bookings'; // + /{id}/cancel
}
