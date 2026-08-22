"use client";

import React from 'react';
import Navbar from '@/components/Navbar';
import StatCard from '@/components/StatCard';
import LiveFleetMap from '@/components/LiveFleetMap';
import { 
  Car, 
  Users, 
  TrendingUp, 
  ShieldAlert, 
  QrCode, 
  CheckCircle2, 
  Clock, 
  ArrowUpRight 
} from 'lucide-react';
import Link from 'next/link';

export default function DashboardOverview() {
  return (
    <div className="flex-1 flex flex-col">
      <Navbar 
        title="Mobility Command Center" 
        subtitle="Real-time shared auto fleet telemetry, commute partner matching & safety monitoring"
      />

      <div className="p-8 space-y-8">
        {/* KPI Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5">
          <StatCard
            title="Active Shared Trips"
            value="142"
            change="+18% vs yesterday"
            isPositive={true}
            icon={Car}
            colorClass="text-emerald-400 bg-emerald-500/10 border-emerald-500/20"
          />
          <StatCard
            title="Online Auto Drivers"
            value="286"
            change="94% KYC Verified"
            isPositive={true}
            icon={Users}
            colorClass="text-blue-400 bg-blue-500/10 border-blue-500/20"
          />
          <StatCard
            title="Daily Commute Matches"
            value="1,840"
            change="87% Mutual Acceptance"
            isPositive={true}
            icon={TrendingUp}
            colorClass="text-purple-400 bg-purple-500/10 border-purple-500/20"
          />
          <StatCard
            title="Active SOS & Safety"
            value="0 Alerts"
            change="All Corridors Safe"
            isPositive={true}
            icon={ShieldAlert}
            colorClass="text-emerald-400 bg-emerald-500/10 border-emerald-500/20"
          />
        </div>

        {/* Live Telemetry Map */}
        <LiveFleetMap />

        {/* Two-Column Operational Section */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Module 1: Live Shared Auto Boarding Feed */}
          <div className="bg-dark-card border border-dark-border rounded-xl p-6 shadow-sm">
            <div className="flex items-center justify-between pb-4 border-b border-dark-border">
              <div className="flex items-center gap-2">
                <QrCode className="w-5 h-5 text-emerald-400" />
                <h3 className="text-sm font-bold text-white">Recent QR Boarding Scans</h3>
              </div>
              <span className="text-xs text-slate-400">HMAC-SHA256 Encrypted</span>
            </div>

            <div className="mt-4 space-y-3">
              {[
                { code: 'RB-84920', passenger: 'Rahul Sharma', route: 'Millennium Metro ➔ Cyber Hub', fare: '₹20', time: '1 min ago', status: 'BOARDED' },
                { code: 'RB-84921', passenger: 'Sneha Patel', route: 'IFFCO Chowk ➔ Cyber City', fare: '₹15', time: '3 mins ago', status: 'BOARDED' },
                { code: 'RB-84922', passenger: 'Amit Verma', route: 'Huda City ➔ Golf Course Rd', fare: '₹25', time: '5 mins ago', status: 'CONFIRMED' },
              ].map((item, idx) => (
                <div key={idx} className="flex items-center justify-between p-3 rounded-lg bg-slate-900/60 border border-slate-800/80 text-xs">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-white">{item.code}</span>
                      <span className="text-slate-400">• {item.passenger}</span>
                    </div>
                    <p className="text-slate-400 mt-0.5">{item.route}</p>
                  </div>
                  <div className="text-right">
                    <span className="font-bold text-emerald-400">{item.fare}</span>
                    <p className="text-[11px] text-slate-500">{item.time}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Module 2: Daily Commute Partner Matches */}
          <div className="bg-dark-card border border-dark-border rounded-xl p-6 shadow-sm">
            <div className="flex items-center justify-between pb-4 border-b border-dark-border">
              <div className="flex items-center gap-2">
                <Users className="w-5 h-5 text-purple-400" />
                <h3 className="text-sm font-bold text-white">Commute Partner AI Handshakes</h3>
              </div>
              <Link href="/commuters" className="text-xs text-brand-400 hover:underline flex items-center gap-1">
                View All <ArrowUpRight className="w-3.5 h-3.5" />
              </Link>
            </div>

            <div className="mt-4 space-y-3">
              {[
                { pair: 'Ananya S. & Priya V.', org: 'TechCorp Solutions', score: '94% AI Match', corridor: 'Sec 29 ➔ Cyber Hub', days: 'Mon-Fri', status: 'ACCEPTED' },
                { pair: 'Vikram M. & Rohan K.', org: 'FinTech Towers', score: '88% AI Match', corridor: 'Sohna Rd ➔ Golf Course Ext', days: 'Mon-Fri', status: 'REQUESTED' },
                { pair: 'Neha G. & Divya R.', org: 'Delhi University Campus', score: '91% AI Match', corridor: 'Rohini ➔ North Campus', days: 'Mon-Sat', status: 'ACCEPTED' },
              ].map((item, idx) => (
                <div key={idx} className="flex items-center justify-between p-3 rounded-lg bg-slate-900/60 border border-slate-800/80 text-xs">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-white">{item.pair}</span>
                      <span className="px-2 py-0.5 rounded text-[10px] bg-purple-500/20 text-purple-300 font-medium">
                        {item.org}
                      </span>
                    </div>
                    <p className="text-slate-400 mt-0.5">{item.corridor} • {item.days}</p>
                  </div>
                  <div className="text-right">
                    <span className="font-bold text-brand-400">{item.score}</span>
                    <p className="text-[11px] text-emerald-400 font-semibold">{item.status}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
