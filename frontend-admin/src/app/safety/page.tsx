"use client";

import React, { useState } from 'react';
import Navbar from '@/components/Navbar';
import { ShieldAlert, PhoneCall, CheckCircle, AlertTriangle, MapPin, Radio } from 'lucide-react';

interface SafetyAlert {
  id: string;
  type: 'SOS_BUTTON' | 'ROUTE_DEVIATION' | 'PROLONGED_STOP';
  userName: string;
  userPhone: string;
  tripId: string;
  driverName: string;
  location: string;
  coordinates: string;
  time: string;
  emergencyContactsNotified: boolean;
  status: 'ACTIVE' | 'DISPATCHED' | 'RESOLVED';
}

const mockAlerts: SafetyAlert[] = [
  {
    id: 'ALT-101',
    type: 'SOS_BUTTON',
    userName: 'Kavita Menon',
    userPhone: '+91 9911882233',
    tripId: 'TRIP-8841',
    driverName: 'Ramesh Kumar (DL 1RN 5521)',
    location: 'Near Shankar Chowk Flyover, Cyber City',
    coordinates: '28.4912° N, 77.0886° E',
    time: '2 mins ago',
    emergencyContactsNotified: true,
    status: 'ACTIVE'
  },
  {
    id: 'ALT-102',
    type: 'ROUTE_DEVIATION',
    userName: 'Automated Telemetry Bot',
    userPhone: 'N/A',
    tripId: 'TRIP-8835',
    driverName: 'Manoj Yadav (HR 26 EV 8812)',
    location: 'Off-corridor by 280m on Golf Course Ext Rd',
    coordinates: '28.4310° N, 77.0980° E',
    time: '12 mins ago',
    emergencyContactsNotified: false,
    status: 'DISPATCHED'
  }
];

export default function SafetyConsolePage() {
  const [alerts, setAlerts] = useState<SafetyAlert[]>(mockAlerts);

  const handleResolve = (id: string) => {
    setAlerts(alerts.map(a => a.id === id ? { ...a, status: 'RESOLVED' } : a));
  };

  const handleDispatch = (id: string) => {
    setAlerts(alerts.map(a => a.id === id ? { ...a, status: 'DISPATCHED' } : a));
  };

  return (
    <div className="flex-1 flex flex-col">
      <Navbar 
        title="Safety & Emergency Response Console" 
        subtitle="24/7 Rapid SOS dispatch, live location broadcasting & corridor deviation triggers"
      />

      <div className="p-8 space-y-6">
        {/* Active Incident Banner */}
        <div className="bg-red-950/40 border border-red-500/50 rounded-xl p-5 flex items-center justify-between shadow-lg">
          <div className="flex items-center gap-3">
            <div className="p-3 bg-red-500/20 border border-red-500/40 rounded-xl sos-alert-badge">
              <ShieldAlert className="w-6 h-6 text-red-400" />
            </div>
            <div>
              <h3 className="text-base font-bold text-white flex items-center gap-2">
                Emergency SOS Dispatch Active 
                <span className="px-2 py-0.5 text-xs bg-red-500 text-white font-extrabold rounded-full">HIGH PRIORITY</span>
              </h3>
              <p className="text-xs text-red-200 mt-0.5">
                Automated SMS & live GPS tracking links have been broadcast to registered emergency contacts and local PCR.
              </p>
            </div>
          </div>
          <button className="px-4 py-2 bg-red-600 hover:bg-red-500 text-white font-bold text-xs rounded-lg transition-colors shadow-md flex items-center gap-1.5">
            <PhoneCall className="w-4 h-4" /> Call Police Control Room (112)
          </button>
        </div>

        {/* Incidents Queue */}
        <div className="bg-dark-card border border-dark-border rounded-xl overflow-hidden shadow-sm">
          <div className="p-4 bg-slate-900/80 border-b border-dark-border flex items-center justify-between">
            <h4 className="text-sm font-bold text-white">Live Incident Stream</h4>
            <span className="text-xs text-slate-400">WebSocket STOMP Channel: /topic/safety.alerts</span>
          </div>

          <div className="divide-y divide-dark-border/60">
            {alerts.map((alert) => (
              <div key={alert.id} className="p-5 flex items-center justify-between hover:bg-slate-900/40 transition-colors">
                <div className="space-y-1.5 text-xs">
                  <div className="flex items-center gap-3">
                    <span className="font-mono font-bold text-red-400 bg-red-950/60 px-2 py-0.5 rounded border border-red-800/40">
                      {alert.id}
                    </span>
                    <span className="font-bold text-white text-sm">{alert.type}</span>
                    <span className="text-slate-400">• {alert.time}</span>
                  </div>

                  <p className="text-slate-300 font-medium">
                    Passenger: <strong className="text-white">{alert.userName}</strong> ({alert.userPhone}) | Driver: {alert.driverName}
                  </p>
                  
                  <div className="flex items-center gap-2 text-slate-400">
                    <MapPin className="w-3.5 h-3.5 text-red-400" />
                    <span>{alert.location} ({alert.coordinates})</span>
                  </div>

                  {alert.emergencyContactsNotified && (
                    <span className="inline-flex items-center gap-1 text-emerald-400 text-[11px] font-semibold">
                      <CheckCircle className="w-3.5 h-3.5" /> Emergency Contacts Notified via SMS
                    </span>
                  )}
                </div>

                <div className="flex items-center gap-3">
                  {alert.status === 'ACTIVE' && (
                    <button
                      onClick={() => handleDispatch(alert.id)}
                      className="px-3.5 py-1.5 bg-amber-600 hover:bg-amber-500 text-white rounded-lg font-bold text-xs transition-colors shadow-sm"
                    >
                      Dispatch Quick Response
                    </button>
                  )}
                  {alert.status !== 'RESOLVED' ? (
                    <button
                      onClick={() => handleResolve(alert.id)}
                      className="px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-lg font-bold text-xs transition-colors shadow-sm"
                    >
                      Mark Resolved
                    </button>
                  ) : (
                    <span className="text-emerald-400 font-bold text-xs flex items-center gap-1">
                      <CheckCircle className="w-4 h-4" /> Incident Resolved
                    </span>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
