"use client";

import React from 'react';
import Navbar from '@/components/Navbar';
import { MapPin, Navigation, Plus, IndianRupee, Layers } from 'lucide-react';

export default function RoutesPage() {
  const routes = [
    {
      id: 'RTE-1',
      name: 'Millennium Metro ➔ Cyber Hub Express',
      origin: 'Millennium City Centre Metro',
      dest: 'DLF Cyber Hub',
      distance: '5.8 km',
      duration: '18 mins',
      baseFare: '₹20',
      activeAutos: 14,
      stops: ['Millennium City Centre Gate 2', 'IFFCO Chowk Junction', 'IndusInd Bank Cyber City'],
      active: true
    },
    {
      id: 'RTE-2',
      name: 'Huda City Centre ➔ Golf Course Road Tech Parks',
      origin: 'Huda City Centre Station',
      dest: 'One Horizon Center',
      distance: '6.4 km',
      duration: '22 mins',
      baseFare: '₹25',
      activeAutos: 9,
      stops: ['Huda Metro Gate 1', 'Sector 29 Market', 'Genpact Chowk', 'One Horizon Center'],
      active: true
    },
    {
      id: 'RTE-3',
      name: 'Vishwavidyalaya Metro ➔ North Campus Colleges',
      origin: 'Vishwavidyalaya Metro Gate 3',
      dest: 'St. Stephen’s & Hindu College',
      distance: '2.5 km',
      duration: '8 mins',
      baseFare: '₹10',
      activeAutos: 22,
      stops: ['Metro Gate 3', 'Arts Faculty', 'SRCC Gate', 'Hindu College'],
      active: true
    }
  ];

  return (
    <div className="flex-1 flex flex-col">
      <Navbar 
        title="Corridors & Stage Fare Matrices" 
        subtitle="Manage digitized shared auto routes, stage-wise pricing, and PostGIS geofenced pickup zones"
      />

      <div className="p-8 space-y-6">
        <div className="flex items-center justify-between">
          <div className="text-xs text-slate-400">
            Showing <strong className="text-white">{routes.length}</strong> active digitized shared auto corridors
          </div>
          <button className="px-4 py-2 bg-brand-600 hover:bg-brand-500 text-white rounded-lg font-bold text-xs flex items-center gap-1.5 transition-colors shadow-md">
            <Plus className="w-4 h-4" /> Create New Corridor
          </button>
        </div>

        <div className="grid grid-cols-1 gap-5">
          {routes.map((route) => (
            <div key={route.id} className="bg-dark-card border border-dark-border rounded-xl p-6 shadow-sm hover:border-slate-700 transition-all">
              <div className="flex items-center justify-between pb-4 border-b border-dark-border">
                <div className="flex items-center gap-3">
                  <div className="p-2.5 rounded-lg bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                    <Navigation className="w-5 h-5" />
                  </div>
                  <div>
                    <h3 className="text-base font-bold text-white">{route.name}</h3>
                    <p className="text-xs text-slate-400 mt-0.5">
                      {route.origin} ➔ {route.dest}
                    </p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <span className="px-2.5 py-1 rounded-full text-xs font-bold bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                    {route.activeAutos} Autos Active
                  </span>
                </div>
              </div>

              {/* Corridor Metrics */}
              <div className="grid grid-cols-4 gap-4 my-4 text-xs">
                <div className="bg-slate-900/60 p-3 rounded-lg border border-slate-800">
                  <span className="text-slate-400 block">Total Distance</span>
                  <span className="font-bold text-white text-sm">{route.distance}</span>
                </div>
                <div className="bg-slate-900/60 p-3 rounded-lg border border-slate-800">
                  <span className="text-slate-400 block">Avg Duration</span>
                  <span className="font-bold text-white text-sm">{route.duration}</span>
                </div>
                <div className="bg-slate-900/60 p-3 rounded-lg border border-slate-800">
                  <span className="text-slate-400 block">Base Stage Fare</span>
                  <span className="font-bold text-emerald-400 text-sm">{route.baseFare}</span>
                </div>
                <div className="bg-slate-900/60 p-3 rounded-lg border border-slate-800">
                  <span className="text-slate-400 block">Designated Stops</span>
                  <span className="font-bold text-white text-sm">{route.stops.length} Stops</span>
                </div>
              </div>

              {/* Stop Sequence Progression */}
              <div className="pt-2">
                <p className="text-xs font-semibold text-slate-400 mb-2 flex items-center gap-1">
                  <Layers className="w-3.5 h-3.5 text-brand-400" /> Designated Stage Stops:
                </p>
                <div className="flex items-center gap-2 flex-wrap text-xs">
                  {route.stops.map((stop, idx) => (
                    <React.Fragment key={idx}>
                      <span className="px-3 py-1.5 rounded-lg bg-slate-900 border border-slate-800 text-slate-300 font-medium">
                        {idx + 1}. {stop}
                      </span>
                      {idx < route.stops.length - 1 && (
                        <span className="text-slate-600 font-bold">➔</span>
                      )}
                    </React.Fragment>
                  ))}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
