"use client";

import React, { useState } from 'react';
import Navbar from '@/components/Navbar';
import { Check, X, Shield, FileText, CheckCircle2, Clock, Car } from 'lucide-react';

interface DriverKYC {
  id: string;
  name: string;
  phone: string;
  vehiclePlate: string;
  vehicleType: string;
  licenseNumber: string;
  aadhaarMasked: string;
  policeVerified: boolean;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  appliedDate: string;
}

const mockDrivers: DriverKYC[] = [
  {
    id: '1',
    name: 'Rajesh Sharma',
    phone: '+91 9811122233',
    vehiclePlate: 'DL 1RN 5521',
    vehicleType: 'Bajaj Compact RE (CNG Auto)',
    licenseNumber: 'DL-1420180054321',
    aadhaarMasked: 'XXXX-XXXX-4920',
    policeVerified: true,
    status: 'PENDING',
    appliedDate: 'Today, 10:30 AM'
  },
  {
    id: '2',
    name: 'Manoj Yadav',
    phone: '+91 9822233344',
    vehiclePlate: 'HR 26 EV 8812',
    vehicleType: 'Mahindra Treo (Electric Auto)',
    licenseNumber: 'HR-2620190012940',
    aadhaarMasked: 'XXXX-XXXX-8119',
    policeVerified: true,
    status: 'PENDING',
    appliedDate: 'Today, 09:15 AM'
  },
  {
    id: '3',
    name: 'Santosh Kumar',
    phone: '+91 9833344455',
    vehiclePlate: 'UP 16 AT 7731',
    vehicleType: 'Piaggio Ape City (CNG Auto)',
    licenseNumber: 'UP-1620170098412',
    aadhaarMasked: 'XXXX-XXXX-2301',
    policeVerified: false,
    status: 'APPROVED',
    appliedDate: 'Yesterday'
  }
];

export default function DriversKYCPage() {
  const [drivers, setDrivers] = useState<DriverKYC[]>(mockDrivers);

  const handleApprove = (id: string) => {
    setDrivers(drivers.map(d => d.id === id ? { ...d, status: 'APPROVED' } : d));
  };

  const handleReject = (id: string) => {
    setDrivers(drivers.map(d => d.id === id ? { ...d, status: 'REJECTED' } : d));
  };

  return (
    <div className="flex-1 flex flex-col">
      <Navbar 
        title="Driver Onboarding & KYC Queue" 
        subtitle="Verify driving licenses, vehicle RC permits, and police background verifications"
      />

      <div className="p-8 space-y-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <span className="px-3 py-1 bg-amber-500/20 text-amber-300 border border-amber-500/30 rounded-lg text-xs font-semibold">
              {drivers.filter(d => d.status === 'PENDING').length} Verification Requests Pending
            </span>
          </div>
        </div>

        {/* Drivers Table */}
        <div className="bg-dark-card border border-dark-border rounded-xl overflow-hidden shadow-sm">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-900/80 text-slate-400 font-semibold uppercase tracking-wider border-b border-dark-border">
                <tr>
                  <th className="py-3.5 px-4">Driver Profile</th>
                  <th className="py-3.5 px-4">Vehicle Details</th>
                  <th className="py-3.5 px-4">DL & Documents</th>
                  <th className="py-3.5 px-4">Police Verification</th>
                  <th className="py-3.5 px-4">Status</th>
                  <th className="py-3.5 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-dark-border/60">
                {drivers.map((driver) => (
                  <tr key={driver.id} className="hover:bg-slate-900/40 transition-colors">
                    <td className="py-4 px-4">
                      <div>
                        <p className="font-bold text-white text-sm">{driver.name}</p>
                        <p className="text-slate-400">{driver.phone}</p>
                      </div>
                    </td>
                    <td className="py-4 px-4">
                      <div>
                        <span className="font-bold text-slate-200">{driver.vehiclePlate}</span>
                        <p className="text-slate-400">{driver.vehicleType}</p>
                      </div>
                    </td>
                    <td className="py-4 px-4">
                      <div>
                        <span className="font-mono text-slate-300">{driver.licenseNumber}</span>
                        <p className="text-slate-400">Aadhaar: {driver.aadhaarMasked}</p>
                      </div>
                    </td>
                    <td className="py-4 px-4">
                      {driver.policeVerified ? (
                        <span className="inline-flex items-center gap-1 text-emerald-400 font-medium">
                          <CheckCircle2 className="w-3.5 h-3.5" /> Verified
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 text-amber-400 font-medium">
                          <Clock className="w-3.5 h-3.5" /> Pending Police Clearance
                        </span>
                      )}
                    </td>
                    <td className="py-4 px-4">
                      <span className={`px-2.5 py-1 rounded-full text-[11px] font-bold ${
                        driver.status === 'APPROVED' ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30' :
                        driver.status === 'REJECTED' ? 'bg-red-500/20 text-red-300 border border-red-500/30' :
                        'bg-amber-500/20 text-amber-300 border border-amber-500/30'
                      }`}>
                        {driver.status}
                      </span>
                    </td>
                    <td className="py-4 px-4 text-right">
                      {driver.status === 'PENDING' ? (
                        <div className="flex items-center justify-end gap-2">
                          <button
                            onClick={() => handleApprove(driver.id)}
                            className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-lg font-semibold flex items-center gap-1 transition-colors shadow-sm"
                          >
                            <Check className="w-3.5 h-3.5" /> Approve
                          </button>
                          <button
                            onClick={() => handleReject(driver.id)}
                            className="px-3 py-1.5 bg-red-600/80 hover:bg-red-600 text-white rounded-lg font-semibold flex items-center gap-1 transition-colors"
                          >
                            <X className="w-3.5 h-3.5" /> Reject
                          </button>
                        </div>
                      ) : (
                        <span className="text-slate-500 italic">Completed</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
