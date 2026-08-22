"use client";

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { 
  LayoutDashboard, 
  Car, 
  MapPin, 
  Users, 
  ShieldAlert, 
  BarChart3, 
  CreditCard, 
  LifeBuoy,
  Navigation
} from 'lucide-react';

export default function Sidebar() {
  const pathname = usePathname();

  const navItems = [
    { label: 'Overview', href: '/', icon: LayoutDashboard },
    { label: 'KYC & Drivers', href: '/drivers', icon: Car },
    { label: 'Corridors & Routes', href: '/routes', icon: MapPin },
    { label: 'Commute Partners', href: '/commuters', icon: Users },
    { label: 'Safety & SOS Console', href: '/safety', icon: ShieldAlert, badge: 'Live' },
    { label: 'AI Demand & Heatmaps', href: '/analytics', icon: BarChart3 },
    { label: 'Financials & Payouts', href: '/payouts', icon: CreditCard },
  ];

  return (
    <aside className="w-64 bg-dark-surface border-r border-dark-border flex flex-col h-screen sticky top-0">
      {/* Brand Header */}
      <div className="p-6 border-b border-dark-border flex items-center gap-3">
        <div className="w-10 h-10 rounded-xl bg-brand-500 flex items-center justify-center shadow-lg shadow-brand-500/30">
          <Navigation className="w-6 h-6 text-white" />
        </div>
        <div>
          <h1 className="text-xl font-bold text-white tracking-tight flex items-center gap-1">
            Rout<span className="text-brand-400">Buddy</span>
          </h1>
          <p className="text-xs text-slate-400 font-medium">Mobility Operations</p>
        </div>
      </div>

      {/* Navigation Links */}
      <nav className="flex-1 px-4 py-6 space-y-1.5 overflow-y-auto">
        <div className="px-3 pb-2 text-xs font-semibold text-slate-500 uppercase tracking-wider">
          Management
        </div>
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive = pathname === item.href;

          return (
            <Link
              key={item.href}
              href={item.href}
              className={`flex items-center justify-between px-3.5 py-2.5 rounded-lg text-sm font-medium transition-all ${
                isActive
                  ? 'bg-brand-600/20 text-brand-400 border border-brand-500/30 shadow-sm'
                  : 'text-slate-300 hover:bg-slate-800/60 hover:text-white'
              }`}
            >
              <div className="flex items-center gap-3">
                <Icon className={`w-5 h-5 ${isActive ? 'text-brand-400' : 'text-slate-400'}`} />
                <span>{item.label}</span>
              </div>
              {item.badge && (
                <span className="px-2 py-0.5 text-xs font-semibold rounded-full bg-red-500/20 text-red-400 border border-red-500/40 sos-alert-badge">
                  {item.badge}
                </span>
              )}
            </Link>
          );
        })}
      </nav>

      {/* User / System Status Footer */}
      <div className="p-4 border-t border-dark-border bg-slate-900/40">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-full bg-slate-800 border border-slate-700 flex items-center justify-center font-bold text-sm text-brand-400">
            SA
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-xs font-semibold text-white truncate">Super Administrator</p>
            <div className="flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
              <p className="text-[11px] text-slate-400">PostGIS & Redis Live</p>
            </div>
          </div>
        </div>
      </div>
    </aside>
  );
}
