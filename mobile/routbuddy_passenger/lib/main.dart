import 'package:flutter/material.dart';
import 'core/storage/token_storage.dart';
import 'features/auth/presentation/login_screen.dart';
import 'features/commute_partner/presentation/commute_partner_screen.dart';
import 'features/safety/presentation/safety_profile_screen.dart';
import 'features/shared_auto/presentation/boarding_pass_screen.dart';
import 'features/shared_auto/presentation/shared_auto_search_screen.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final bool loggedIn = await TokenStorage.isLoggedIn();
  runApp(RoutBuddyPassengerApp(isLoggedIn: loggedIn));
}

class RoutBuddyPassengerApp extends StatelessWidget {
  final bool isLoggedIn;

  const RoutBuddyPassengerApp({Key? key, required this.isLoggedIn}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'RoutBuddy Passenger',
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
      home: isLoggedIn ? const MainNavigationShell() : const LoginScreen(),
    );
  }
}

class MainNavigationShell extends StatefulWidget {
  const MainNavigationShell({Key? key}) : super(key: key);

  @override
  State<MainNavigationShell> createState() => _MainNavigationShellState();
}

class _MainNavigationShellState extends State<MainNavigationShell> {
  int _currentIndex = 0;

  final List<Widget> _screens = const [
    SharedAutoSearchScreen(),
    CommutePartnerScreen(),
    BoardingPassScreen(),
    SafetyProfileScreen(),
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
            icon: Icon(Icons.directions_car_outlined),
            selectedIcon: Icon(Icons.directions_car, color: Color(0xFF10B981)),
            label: 'Shared Auto',
          ),
          NavigationDestination(
            icon: Icon(Icons.people_outline),
            selectedIcon: Icon(Icons.people, color: Color(0xFF10B981)),
            label: 'Commute Partner',
          ),
          NavigationDestination(
            icon: Icon(Icons.qr_code_2_outlined),
            selectedIcon: Icon(Icons.qr_code_2, color: Color(0xFF10B981)),
            label: 'Boarding Pass',
          ),
          NavigationDestination(
            icon: Icon(Icons.shield_outlined),
            selectedIcon: Icon(Icons.shield, color: Color(0xFF10B981)),
            label: 'Safety & Trust',
          ),
        ],
      ),
    );
  }
}
