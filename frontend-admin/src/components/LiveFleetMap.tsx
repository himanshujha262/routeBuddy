"use client";

import React, { useState } from 'react';
import { Navigation, Users, Zap, Shield, Compass, Radio } from 'lucide-react';

interface ActiveVehicle {
  id: string;
  driverName: string;
  vehiclePlate: string;
  route: string;
  occupancy: string;
  speed: string;
  batteryOrFuel: string;
  status: 'BOARDING' | 'EN_ROUTE' | 'SCHEDULED';
  coordinates: [number, number];
}

const mockVehicles: ActiveVehicle[] = [
  {
    id: '1',
    driverName: 'Ramesh Kumar',
    vehiclePlate: 'DL 1RN 5521',
    route: 'Millennium Metro ➔ Cyber Hub Express',
    occupancy: '2 / 3 Seats',
    speed: '24 km/h',
    batteryOrFuel: 'CNG 85%',
    status: 'EN_ROUTE',
    coordinates: [28.4725, 77.0697],
  },
  {
    id: '2',
    driverName: 'Suresh Yadav',
    vehiclePlate: 'DL 1EV 9940',
    route: 'Huda City ➔ Golf Course Rd Tech Park',
    occupancy: '3 / 4 Seats',
    speed: '18 km/h',
    batteryOrFuel: 'EV 78%',
    status: 'BOARDING',
    coordinates: [28.4595, 77.0725],
  },
  {
    id: '3',
    driverName: 'Mohd. Imran',
    vehiclePlate: 'HR 26 BY 3311',
    route: 'Sector 55 Metro ➔ Cyber City Shuttle',
    occupancy: '5 / 6 Seats',
    speed: '32 km/h',
    batteryOrFuel: 'CNG 60%',
    status: 'EN_ROUTE',
    coordinates: [28.4890, 77.0865],
  }
];

export default function LiveFleetMap() {
  const [selectedVehicle, setSelectedVehicle] = useState<ActiveVehicle>(mockVehicles[0]);

  return (
    <div className="bg-dark-card border border-dark-border rounded-xl overflow-hidden shadow-lg flex flex-col h-[520px]">
      {/* Map Header */}
      <div className="p-4 bg-slate-900/80 border-b border-dark-border flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Radio className="w-4 h-4 text-emerald-400 animate-pulse" />
          <h3 className="text-sm font-bold text-white">Live Telemetry & Shared Corridor Map</h3>
        </div>
        <div className="flex items-center gap-3 text-xs text-slate-400">
          <span className="flex items-center gap-1">
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-500"></span> En Route ({mockVehicles.filter(v => v.status === 'EN_ROUTE').length})
          </span>
          <span className="flex items-center gap-1">
            <span className="w-2.5 h-2.5 rounded-full bg-amber-500"></span> Boarding ({mockVehicles.filter(v => v.status === 'BOARDING').length})
          </span>
        </div>
      </div>

      {/* Map Canvas / Simulated GIS View */}
      <div className="flex-1 relative bg-slate-950 flex">
        {/* Visual Map Grid & Corridor Lines */}
        <div className="flex-1 relative overflow-hidden bg-[#0a0f1d] p-6 flex flex-col justify-between">
          <div className="absolute inset-0 opacity-20 bg-[radial-gradient(#334155_1px,transparent_1px)] [background-size:16px_16px]"></div>

          {/* Simulated Corridor Line */}
          <div className="relative z-10 w-full h-full border border-dashed border-emerald-500/30 rounded-2xl p-6 flex flex-col justify-between">
            <div className="flex items-center justify-between">
              <div className="bg-slate-900/90 border border-slate-700 px-3 py-1.5 rounded-lg text-xs font-semibold text-emerald-400 flex items-center gap-2">
                <Navigation className="w-3.5 h-3.5" />
                <span>Origin: Millennium City Centre (Hub A)</span>
              </div>
              <div className="bg-slate-900/90 border border-slate-700 px-3 py-1.5 rounded-lg text-xs font-semibold text-emerald-400 flex items-center gap-2">
                <Compass className="w-3.5 h-3.5" />
                <span>Dest: DLF Cyber Hub (Hub B)</span>
              </div>
            </div>

            {/* Simulated Live Vehicle Markers */}
            <div className="grid grid-cols-3 gap-4 my-auto">
              {mockVehicles.map((v) => {
                const isSelected = selectedVehicle.id === v.id;
                return (
                  <button
                    key={v.id}
                    onClick={() => setSelectedVehicle(v)}
                    className={`p-4 rounded-xl text-left border transition-all ${
                      isSelected
                        ? 'bg-emerald-950/50 border-emerald-500 shadow-lg shadow-emerald-500/20'
                        : 'bg-slate-900/80 border-slate-800 hover:border-slate-700'
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-bold text-white">{v.vehiclePlate}</span>
                      <span className={`px-2 py-0.5 text-[10px] font-bold rounded-full ${
                        v.status === 'EN_ROUTE' ? 'bg-emerald-500/20 text-emerald-300' : 'bg-amber-500/20 text-amber-300'
                      }`}>
                        {v.status}
                      </span>
                    </div>
                    <p className="text-xs text-slate-300 font-medium mt-1">{v.driverName}</p>
                    <div className="mt-3 flex items-center justify-between text-[11px] text-slate-400">
                      <span className="flex items-center gap-1 text-emerald-400">
                        <Users className="w-3 h-3" /> {v.occupancy}
                      </span>
                      <span>{v.speed}</span>
                    </div>
                  </button>
                );
              })}
            </div>

            <div className="text-center text-xs text-slate-500">
              Corridor Geofence Buffer: 150m | PostGIS LineString SRID 4326 | Real-time Redis STOMP Stream
            </div>
          </div>
        </div>

        {/* Selected Vehicle Telemetry Drawer */}
        <div className="w-80 bg-slate-900 border-l border-dark-border p-5 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <div>
                <h4 className="text-sm font-bold text-white">{selectedVehicle.driverName}</h4>
                <p className="text-xs text-slate-400">{selectedVehicle.vehiclePlate}</p>
              </div>
              <span className="p-2 rounded-lg bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                <Zap className="w-4 h-4" />
              </span>
            </div>

            <div className="mt-4 space-y-3 text-xs">
              <div>
                <span className="text-slate-500 block">Active Corridor</span>
                <span className="font-semibold text-slate-200">{selectedVehicle.route}</span>
              </div>
              <div className="flex justify-between">
                <div>
                  <span className="text-slate-500 block">Seat Occupancy</span>
                  <span className="font-semibold text-emerald-400">{selectedVehicle.occupancy}</span>
                </div>
                <div>
                  <span className="text-slate-500 block">GPS Speed</span>
                  <span className="font-semibold text-slate-200">{selectedVehicle.speed}</span>
                </div>
              </div>
              <div>
                <span className="text-slate-500 block">Fuel / Power</span>
                <span className="font-semibold text-slate-200">{selectedVehicle.batteryOrFuel}</span>
              </div>
              <div>
                <span className="text-slate-500 block">Corridor Deviation Status</span>
                <span className="font-semibold text-emerald-400 flex items-center gap-1">
                  <Shield className="w-3 h-3" /> On Track (0m deviation)
                </span>
              </div>
            </div>
          </div>

          <div className="pt-4 border-t border-slate-800">
            <button className="w-full py-2 px-3 rounded-lg bg-brand-600 hover:bg-brand-500 text-white font-medium text-xs transition-colors shadow-md">
              View Driver Telemetry Log
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
