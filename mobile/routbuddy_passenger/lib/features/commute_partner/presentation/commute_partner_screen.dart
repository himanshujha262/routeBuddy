import 'package:flutter/material.dart';

class CommutePartnerScreen extends StatefulWidget {
  const CommutePartnerScreen({Key? key}) : super(key: key);

  @override
  State<CommutePartnerScreen> createState() => _CommutePartnerScreenState();
}

class _CommutePartnerScreenState extends State<CommutePartnerScreen> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Daily Commute Partners', style: TextStyle(fontWeight: FontWeight.bold)),
        actions: [
          IconButton(
            icon: const Icon(Icons.tune),
            onPressed: () {},
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // User's Active Corridor Setup Banner
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  colors: [const Color(0xFF10B981).withOpacity(0.15), const Color(0xFF182234)],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: const Color(0xFF10B981).withOpacity(0.3)),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text('MY COMMUTE CORRIDOR', style: TextStyle(color: Color(0xFF10B981), fontSize: 10, fontWeight: FontWeight.bold, letterSpacing: 1.1)),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                        decoration: BoxDecoration(
                          color: const Color(0xFF10B981).withOpacity(0.2),
                          borderRadius: BorderRadius.circular(4),
                        ),
                        child: const Text('OFFICE PLAN (MON-FRI)', style: TextStyle(color: Color(0xFF10B981), fontSize: 9, fontWeight: FontWeight.bold)),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  const Text('Sector 28/29, Gurgaon ➔ DLF Cyber Hub', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 14)),
                  const SizedBox(height: 4),
                  const Text('Departs 08:50 AM • Returns 06:15 PM • Female-only preference', style: TextStyle(color: Colors.grey, fontSize: 11)),
                ],
              ),
            ),
            const SizedBox(height: 20),

            // AI Match Feed Section
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: const [
                Expanded(
                  child: Text(
                    'AI Recommended Partners',
                    style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold, color: Colors.white),
                  ),
                ),
                SizedBox(width: 8),
                Text('3 Matches Found', style: TextStyle(color: Color(0xFF10B981), fontSize: 12, fontWeight: FontWeight.bold)),
              ],
            ),
            const SizedBox(height: 12),

            // Commuter Match Cards
            _buildCommuterCard(
              name: 'Ananya Sharma',
              rating: '4.9 ★',
              orgName: 'TechCorp Solutions',
              isOrgVerified: true,
              matchScore: '94% AI Match',
              commuteRole: 'Offering Ride (Honda City)',
              neighborhood: 'Sector 29, Gurgaon (~400m away)',
              departure: '08:45 AM - 09:15 AM',
              days: 'Mon, Tue, Wed, Thu, Fri',
              status: 'ACCEPTED',
              context: context,
            ),
            const SizedBox(height: 12),
            _buildCommuterCard(
              name: 'Pooja Kapoor',
              rating: '4.8 ★',
              orgName: 'FinTech Hub',
              isOrgVerified: true,
              matchScore: '89% AI Match',
              commuteRole: 'Seeking Co-Commuter',
              neighborhood: 'Sector 28, Gurgaon (~600m away)',
              departure: '09:00 AM - 09:30 AM',
              days: 'Mon, Tue, Wed, Thu, Fri',
              status: 'SUGGESTED',
              context: context,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildCommuterCard({
    required String name,
    required String rating,
    required String orgName,
    required bool isOrgVerified,
    required String matchScore,
    required String commuteRole,
    required String neighborhood,
    required String departure,
    required String days,
    required String status,
    required BuildContext context,
  }) {
    return Container(
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
              Row(
                children: [
                  CircleAvatar(
                    backgroundColor: Colors.purple.withOpacity(0.2),
                    child: const Icon(Icons.person, color: Colors.purpleAccent),
                  ),
                  const SizedBox(width: 12),
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Text(name, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14, color: Colors.white)),
                          const SizedBox(width: 6),
                          if (isOrgVerified)
                            const Icon(Icons.verified, color: Color(0xFF10B981), size: 14),
                        ],
                      ),
                      Text(orgName, style: const TextStyle(color: Colors.grey, fontSize: 11)),
                    ],
                  ),
                ],
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                decoration: BoxDecoration(
                  color: const Color(0xFF10B981).withOpacity(0.2),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Text(matchScore, style: const TextStyle(color: Color(0xFF10B981), fontWeight: FontWeight.bold, fontSize: 11)),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: const Color(0xFF111827),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Column(
              children: [
                Row(
                  children: [
                    const Icon(Icons.location_on_outlined, color: Colors.grey, size: 14),
                    const SizedBox(width: 6),
                    Text(neighborhood, style: const TextStyle(color: Colors.grey, fontSize: 11)),
                  ],
                ),
                const SizedBox(height: 6),
                Row(
                  children: [
                    const Icon(Icons.schedule, color: Colors.grey, size: 14),
                    const SizedBox(width: 6),
                    Text('$departure • $days', style: const TextStyle(color: Colors.grey, fontSize: 11)),
                  ],
                ),
              ],
            ),
          ),
          const SizedBox(height: 12),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(commuteRole, style: const TextStyle(color: Colors.white70, fontSize: 11, fontWeight: FontWeight.w500)),
              if (status == 'ACCEPTED')
                ElevatedButton.icon(
                  onPressed: () {
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('Opening secure in-app chat channel...')),
                    );
                  },
                  icon: const Icon(Icons.chat_bubble_outline, size: 14),
                  label: const Text('Chat & Coordinate'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFF10B981),
                    foregroundColor: Colors.white,
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                  ),
                )
              else
                ElevatedButton(
                  onPressed: () {
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('Commute partner invite sent! Awaiting mutual acceptance.')),
                    );
                  },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFF3B82F6),
                    foregroundColor: Colors.white,
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                  ),
                  child: const Text('Send Match Request'),
                ),
            ],
          ),
        ],
      ),
    );
  }
}
