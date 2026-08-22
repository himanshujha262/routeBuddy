import 'package:shared_preferences/shared_preferences.dart';

class TokenStorage {
  static const String _accessTokenKey = 'driver_access_token';
  static const String _refreshTokenKey = 'driver_refresh_token';
  static const String _userIdKey = 'driver_user_id';
  static const String _driverIdKey = 'driver_profile_id';
  static const String _userNameKey = 'driver_user_name';
  static const String _userPhoneKey = 'driver_user_phone';

  static Future<void> saveSession({
    required String accessToken,
    required String refreshToken,
    required String userId,
    required String fullName,
    required String phone,
  }) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_accessTokenKey, accessToken);
    await prefs.setString(_refreshTokenKey, refreshToken);
    await prefs.setString(_userIdKey, userId);
    await prefs.setString(_userNameKey, fullName);
    await prefs.setString(_userPhoneKey, phone);
  }

  static Future<void> saveDriverProfileId(String driverId) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_driverIdKey, driverId);
  }

  static Future<String?> getAccessToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_accessTokenKey);
  }

  static Future<String?> getUserId() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_userIdKey);
  }

  static Future<String?> getDriverProfileId() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_driverIdKey);
  }

  static Future<String?> getUserName() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_userNameKey);
  }

  static Future<bool> isLoggedIn() async {
    final token = await getAccessToken();
    return token != null && token.isNotEmpty;
  }

  static Future<void> clear() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.clear();
  }
}
