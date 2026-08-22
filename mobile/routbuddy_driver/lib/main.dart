import 'package:flutter/material.dart';
import 'core/storage/token_storage.dart';
import 'features/auth/presentation/driver_login_screen.dart';
import 'features/earnings/presentation/driver_earnings_screen.dart';
import 'features/qr_scanner/presentation/driver_qr_scanner_screen.dart';
import 'features/trip_management/presentation/driver_trip_screen.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final bool loggedIn = await TokenStorage.isLoggedIn();
  runApp(RoutBuddyDriverApp(isLoggedIn: loggedIn));
}

class RoutBuddyDriverApp extends StatelessWidget {
  final bool isLoggedIn;

  const RoutBuddyDriverApp({Key? key, required this.isLoggedIn}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'RoutBuddy Driver',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        brightness: Brightness.dark,
        scaffoldBackgroundColor: const Color(0xFF0B0F19),
        colorScheme: const ColorScheme.dark(
          primary: Color(0xFF10B981), // Emerald
          secondary: Color(0xFF059669),
          surface: Color(0xFF111827),
        ),
        cardColor: const Color(0xFF182234),
        appBarTheme: const AppBarTheme(
          backgroundColor: Color(0xFF111827),
          elevation: 0,
          centerTitle: false,
        ),
        useMaterial3: true,
      ),
      home: isLoggedIn ? const DriverMainNavigationShell() : const DriverLoginScreen(),
    );
  }
}

class DriverMainNavigationShell extends StatefulWidget {
  const DriverMainNavigationShell({Key? key}) : super(key: key);

  @override
  State<DriverMainNavigationShell> createState() => _DriverMainNavigationShellState();
}

class _DriverMainNavigationShellState extends State<DriverMainNavigationShell> {
  int _currentIndex = 0;

  final List<Widget> _screens = const [
    DriverTripScreen(),
    DriverQrScannerScreen(),
    DriverEarningsScreen(),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: _screens[_currentIndex],
      bottomNavigationBar: NavigationBar(
        selectedIndex: _currentIndex,
        onDestinationSelected: (index) => setState(() => _currentIndex = index),
        backgroundColor: const Color(0xFF111827),
        indicatorColor: const Color(0xFF10B981).withOpacity(0.2),
        destinations: const [
          NavigationDestination(
            icon: Icon(Icons.dashboard_outlined),
            selectedIcon: Icon(Icons.dashboard, color: Color(0xFF10B981)),
            label: 'Dashboard',
          ),
          NavigationDestination(
            icon: Icon(Icons.qr_code_scanner_outlined),
            selectedIcon: Icon(Icons.qr_code_scanner, color: Color(0xFF10B981)),
            label: 'Verify Pass',
          ),
          NavigationDestination(
            icon: Icon(Icons.account_balance_wallet_outlined),
            selectedIcon: Icon(Icons.account_balance_wallet, color: Color(0xFF10B981)),
            label: 'Earnings',
          ),
        ],
      ),
    );
  }
}
