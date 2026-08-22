import 'package:flutter/material.dart';

class SafetyProfileScreen extends StatelessWidget {
  const SafetyProfileScreen({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Safety, Trust & Profile', style: TextStyle(fontWeight: FontWeight.bold)),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            // Big Emergency SOS Button Card
            Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: const Color(0xFF450A0A),
                borderRadius: BorderRadius.circular(20),
                border: Border.all(color: Colors.redAccent.withOpacity(0.5)),
              ),
              child: Column(
                children: [
                  Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.all(10),
                        decoration: BoxDecoration(
                          color: Colors.red.withOpacity(0.2),
                          shape: BoxShape.circle,
                        ),
                        child: const Icon(Icons.shield_outlined, color: Colors.redAccent, size: 28),
                      ),
                      const SizedBox(width: 14),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: const [
                            Text('EMERGENCY SOS', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 16)),
                            Text('Instantly alert emergency contacts & police with live GPS', style: TextStyle(color: Colors.redAccent, fontSize: 11)),
                          ],
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  ElevatedButton.icon(
                    onPressed: () {
                      showDialog(
                        context: context,
                        builder: (ctx) => AlertDialog(
                          backgroundColor: const Color(0xFF182234),
                          title: const Text('Trigger Emergency SOS?', style: TextStyle(color: Colors.redAccent, fontWeight: FontWeight.bold)),
                          content: const Text('This will broadcast your live location to your emergency contacts and the RoutBuddy 24/7 Safety Command Center.', style: TextStyle(color: Colors.white)),
                          actions: [
                            TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
                            ElevatedButton(
                              onPressed: () {
                                Navigator.pop(ctx);
                                ScaffoldMessenger.of(context).showSnackBar(
                                  const SnackBar(backgroundColor: Colors.red, content: Text('SOS Alert Broadcasted! Help is on the way.')),
                                );
                              },
                              style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
                              child: const Text('CONFIRM SOS'),
                            ),
                          ],
                        ),
                      );
                    },
                    icon: const Icon(Icons.warning_amber_rounded),
                    label: const Text('TRIGGER SOS ALERT'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.red,
                      foregroundColor: Colors.white,
                      minimumSize: const Size(double.infinity, 44),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 20),

            // Profile & Trust Score Card
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: const Color(0xFF182234),
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: const Color(0xFF1F2937)),
              ),
              child: Column(
                children: [
                  Row(
                    children: [
                      const CircleAvatar(
                        radius: 28,
                        backgroundColor: Color(0xFF10B981),
                        child: Text('PS', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 18)),
                      ),
                      const SizedBox(width: 14),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              children: const [
                                Text('Priya Sharma', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 16)),
                                SizedBox(width: 6),
                                Icon(Icons.verified, color: Color(0xFF10B981), size: 16),
                              ],
                            ),
                            const Text('+91 9977665544', style: TextStyle(color: Colors.grey, fontSize: 12)),
                            const SizedBox(height: 4),
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                              decoration: BoxDecoration(
                                color: const Color(0xFF10B981).withOpacity(0.2),
                                borderRadius: BorderRadius.circular(4),
                              ),
                              child: const Text('Verified Corporate: TechCorp Solutions', style: TextStyle(color: Color(0xFF10B981), fontSize: 10, fontWeight: FontWeight.bold)),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                  const Divider(color: Color(0xFF1F2937), height: 24),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceAround,
                    children: [
                      _buildProfileStat('Trust Score', '4.9 / 5.0'),
                      _buildProfileStat('Trips Taken', '128 Rides'),
                      _buildProfileStat('Commute Plan', 'Monthly Pass'),
                    ],
                  ),
                ],
              ),
            ),
            const SizedBox(height: 20),

            // Emergency Contacts Section
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
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text('Emergency Contacts', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 14)),
                      TextButton.icon(
                        onPressed: () {},
                        icon: const Icon(Icons.add, size: 16, color: Color(0xFF10B981)),
                        label: const Text('Add', style: TextStyle(color: Color(0xFF10B981), fontSize: 12)),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  ListTile(
                    contentPadding: EdgeInsets.zero,
                    leading: const CircleAvatar(
                      backgroundColor: Color(0xFF1F2937),
                      child: Icon(Icons.phone, color: Color(0xFF10B981), size: 18),
                    ),
                    title: const Text('Rakesh Sharma (Father)', style: TextStyle(color: Colors.white, fontSize: 13, fontWeight: FontWeight.bold)),
                    subtitle: const Text('+91 9811002233 • Primary SOS Contact', style: TextStyle(color: Colors.grey, fontSize: 11)),
                    trailing: const Icon(Icons.check_circle, color: Color(0xFF10B981), size: 18),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildProfileStat(String label, String value) {
    return Column(
      children: [
        Text(label, style: const TextStyle(color: Colors.grey, fontSize: 10)),
        const SizedBox(height: 4),
        Text(value, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 13)),
      ],
    );
  }
}
