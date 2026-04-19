import React from 'react';
const statusConfig = {
    REQUESTED: {
        label: 'Searching',
        color: 'text-amber-400',
        bg: 'bg-amber-400/10 border-amber-400/30',
    },
    DRIVER_ASSIGNED: {
        label: 'Driver Assigned',
        color: 'text-blue-400',
        bg: 'bg-blue-400/10 border-blue-400/30',
    },
    DRIVER_ARRIVED: {
        label: 'Driver Arrived',
        color: 'text-purple-400',
        bg: 'bg-purple-400/10 border-purple-400/30',
    },
    IN_PROGRESS: {
        label: 'In Progress',
        color: 'text-emerald-400',
        bg: 'bg-emerald-400/10 border-emerald-400/30',
    },
    COMPLETED: {
        label: 'Completed',
        color: 'text-emerald-400',
        bg: 'bg-emerald-400/10 border-emerald-400/30',
    },
    CANCELLED: {
        label: 'Cancelled',
        color: 'text-red-400',
        bg: 'bg-red-400/10 border-red-400/30',
    },
};
export default function StatusBadge({ status, size = 'sm', pulse = false }) {
    const cfg = statusConfig[status] || statusConfig.REQUESTED;
    const sizeClasses = size === 'md' ? 'text-sm px-3.5 py-1.5' : 'text-xs px-2.5 py-1';
    return (<span className={`
        inline-flex items-center gap-1.5 font-semibold rounded-full border
        ${cfg.color} ${cfg.bg} ${sizeClasses}
      `}>
      {(pulse || status === 'REQUESTED' || status === 'IN_PROGRESS') && (<span className="relative flex h-2 w-2">
          <span className={`animate-ping absolute inline-flex h-full w-full rounded-full opacity-75 ${status === 'IN_PROGRESS' ? 'bg-emerald-400' :
                status === 'REQUESTED' ? 'bg-amber-400' : 'bg-current'}`}/>
          <span className={`relative inline-flex rounded-full h-2 w-2 ${status === 'IN_PROGRESS' ? 'bg-emerald-400' :
                status === 'REQUESTED' ? 'bg-amber-400' : 'bg-current'}`}/>
        </span>)}
      {cfg.label}
    </span>);
}
