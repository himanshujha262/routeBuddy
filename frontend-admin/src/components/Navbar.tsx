"use client";

import React from 'react';
import { Bell, Search, ShieldCheck, Wifi } from 'lucide-react';

interface NavbarProps {
  title: string;
  subtitle?: string;
}

export default function Navbar({ title, subtitle }: NavbarProps) {
  return (
    <header className="h-16 border-b border-dark-border bg-dark-surface/60 backdrop-blur-md px-8 flex items-center justify-between sticky top-0 z-40">
      <div>
        <h2 className="text-lg font-bold text-white">{title}</h2>
        {subtitle && <p className="text-xs text-slate-400">{subtitle}</p>}
      </div>

      <div className="flex items-center gap-4">
        {/* Search Bar */}
        <div className="relative w-64">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search driver, route, booking..."
            className="w-full bg-slate-900 border border-dark-border rounded-lg pl-9 pr-3 py-1.5 text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-brand-500 transition-colors"
          />
        </div>

        {/* AI & Telemetry Status Badge */}
        <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-emerald-950/40 border border-emerald-800/40 text-emerald-400 text-xs font-medium">
          <Wifi className="w-3.5 h-3.5" />
          <span>AI Engine Active</span>
        </div>

        {/* Notifications */}
        <button className="relative p-2 rounded-lg bg-slate-900 border border-dark-border text-slate-300 hover:text-white transition-colors">
          <Bell className="w-4 h-4" />
          <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-brand-500"></span>
        </button>
      </div>
    </header>
  );
}
