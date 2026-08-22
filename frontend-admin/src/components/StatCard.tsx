import React from 'react';
import { LucideIcon } from 'lucide-react';

interface StatCardProps {
  title: string;
  value: string | number;
  change?: string;
  isPositive?: boolean;
  icon: LucideIcon;
  colorClass?: string;
}

export default function StatCard({
  title,
  value,
  change,
  isPositive,
  icon: Icon,
  colorClass = 'text-brand-400 bg-brand-500/10 border-brand-500/20'
}: StatCardProps) {
  return (
    <div className="bg-dark-card/70 border border-dark-border rounded-xl p-5 shadow-sm backdrop-blur-sm hover:border-slate-700 transition-all">
      <div className="flex items-center justify-between">
        <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider">{title}</p>
        <div className={`p-2.5 rounded-lg border ${colorClass}`}>
          <Icon className="w-5 h-5" />
        </div>
      </div>
      <div className="mt-4 flex items-baseline gap-2">
        <h3 className="text-2xl font-extrabold text-white tracking-tight">{value}</h3>
        {change && (
          <span className={`text-xs font-semibold ${isPositive ? 'text-emerald-400' : 'text-red-400'}`}>
            {change}
          </span>
        )}
      </div>
    </div>
  );
}
