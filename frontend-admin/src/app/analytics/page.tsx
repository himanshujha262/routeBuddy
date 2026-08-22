"use client";

import React from 'react';
import Navbar from '@/components/Navbar';
import { BarChart3, TrendingUp, Zap, Users, ShieldCheck, Flame } from 'lucide-react';

export default function AnalyticsPage() {
  return (
    <div className="flex-1 flex flex-col">
      <Navbar 
        title="AI Demand & Fleet Analytics" 
        subtitle="Real-time transit demand forecasts, corridor occupancy ratios & partner matching conversion metrics"
      />

      <div className="p-8 space-y-8">
        {/* Top Demand Highlights */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
          <div className="bg-dark-card border border-dark-border rounded-xl p-5">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-slate-400 uppercase">Current Fleet Occupancy</span>
              <Users className="w-5 h-5 text-emerald-400" />
            </div>
            <h3 className="text-2xl font-extrabold text-white mt-3">86.4%</h3>
            <p className="text-xs text-emerald-400 font-medium mt-1">2.6 passengers / 3-seater auto average</p>
          </div>

          <div className="bg-dark-card border border-dark-border rounded-xl p-5">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-slate-400 uppercase">Peak Surge Multiplier</span>
              <Flame className="w-5 h-5 text-amber-400" />
            </div>
            <h3 className="text-2xl font-extrabold text-white mt-3">1.25x</h3>
            <p className="text-xs text-amber-300 font-medium mt-1">Millennium Metro Hub (08:30 - 10:00 AM)</p>
          </div>

          <div className="bg-dark-card border border-dark-border rounded-xl p-5">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-slate-400 uppercase">Commute Match Success</span>
              <Zap className="w-5 h-5 text-purple-400" />
            </div>
            <h3 className="text-2xl font-extrabold text-white mt-3">89.2%</h3>
            <p className="text-xs text-purple-300 font-medium mt-1">Double opt-in conversion rate</p>
          </div>
        </div>

        {/* AI Forecast Hotspots */}
        <div className="bg-dark-card border border-dark-border rounded-xl p-6 shadow-sm">
          <div className="flex items-center justify-between pb-4 border-b border-dark-border">
            <div>
              <h3 className="text-sm font-bold text-white flex items-center gap-2">
                <BarChart3 className="w-4 h-4 text-emerald-400" />
                AI Transit Demand Hotspot Forecast (Next 60 Minutes)
              </h3>
              <p className="text-xs text-slate-400 mt-0.5">
                FastAPI XGBoost & spatial temporal model recommendations for auto stand dispatch
              </p>
            </div>
            <span className="px-2.5 py-1 rounded bg-emerald-500/20 text-emerald-300 text-xs font-bold">
              Model Accuracy: 94.2%
            </span>
          </div>

          <div className="mt-5 space-y-4">
            {[
              { stand: 'Millennium City Centre Metro Gate 2', queue: '58 passengers', recommended: '20 autos', surge: '1.2x', level: 'HIGH SURGE' },
              { stand: 'IndusInd Bank Cyber City Interchange', queue: '42 passengers', recommended: '15 autos', surge: '1.1x', level: 'HIGH' },
              { stand: 'IFFCO Chowk Flyover Stand', queue: '25 passengers', recommended: '9 autos', surge: '1.0x', level: 'MODERATE' },
              { stand: 'Sector 55-56 Rapid Metro Stand', queue: '30 passengers', recommended: '10 autos', surge: '1.1x', level: 'HIGH' },
            ].map((hotspot, idx) => (
              <div key={idx} className="flex items-center justify-between p-4 rounded-lg bg-slate-900/60 border border-slate-800 text-xs">
                <div>
                  <h4 className="font-bold text-white text-sm">{hotspot.stand}</h4>
                  <p className="text-slate-400 mt-1">
                    Expected Queue: <strong className="text-slate-200">{hotspot.queue}</strong> | Recommended Fleet: <strong className="text-emerald-400">{hotspot.recommended}</strong>
                  </p>
                </div>
                <div className="text-right">
                  <span className={`px-2.5 py-1 rounded-full text-[11px] font-bold ${
                    hotspot.level.includes('SURGE') ? 'bg-red-500/20 text-red-300 border border-red-500/30' : 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
                  }`}>
                    {hotspot.level}
                  </span>
                  <p className="text-slate-400 mt-1 font-mono">Surge: {hotspot.surge}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
