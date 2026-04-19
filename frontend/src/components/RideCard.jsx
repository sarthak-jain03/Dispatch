import React from 'react';
import { Navigation, DollarSign, ChevronRight } from 'lucide-react';
import StatusBadge from './StatusBadge';
export default function RideCard({ ride, onClick }) {
    const date = ride.requestedAt
        ? new Date(ride.requestedAt).toLocaleDateString('en-IN', {
            day: 'numeric',
            month: 'short',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
        })
        : '—';
    return (<div onClick={() => onClick?.(ride)} className={`
        card group animate-[fade-in_0.3s_ease-out]
        ${onClick ? 'cursor-pointer hover:border-uber-gray-500 hover:bg-uber-gray-800' : ''}
      `}>
      {/* Top row: status + date */}
      <div className="flex items-center justify-between mb-4">
        <StatusBadge status={ride.status}/>
        <span className="text-xs text-uber-gray-400">{date}</span>
      </div>

      {/* Route */}
      <div className="flex gap-3 mb-4">
        {/* Dots & line */}
        <div className="flex flex-col items-center pt-0.5">
          <div className="w-2.5 h-2.5 rounded-full bg-uber-green shrink-0"/>
          <div className="w-px flex-1 bg-uber-gray-600 my-1"/>
          <div className="w-2.5 h-2.5 rounded-sm bg-uber-blue shrink-0"/>
        </div>

        {/* Addresses */}
        <div className="flex-1 min-w-0 space-y-3">
          <div>
            <p className="text-white text-sm font-medium truncate">
              {ride.pickupAddress || `${ride.pickupLatitude.toFixed(4)}, ${ride.pickupLongitude.toFixed(4)}`}
            </p>
          </div>
          <div>
            <p className="text-white text-sm font-medium truncate">
              {ride.dropAddress || `${ride.dropLatitude.toFixed(4)}, ${ride.dropLongitude.toFixed(4)}`}
            </p>
          </div>
        </div>

        {/* Chevron */}
        {onClick && (<ChevronRight className="w-5 h-5 text-uber-gray-500 shrink-0 self-center group-hover:text-white transition-colors"/>)}
      </div>

      {/* Bottom row: fare + distance */}
      <div className="flex items-center gap-4 pt-3 border-t border-uber-gray-700">
        <div className="flex items-center gap-1.5 text-uber-gray-300 text-sm">
          <DollarSign className="w-3.5 h-3.5 text-uber-green"/>
          <span className="font-semibold">₹{(ride.actualFare || ride.estimatedFare).toFixed(0)}</span>
        </div>
        <div className="flex items-center gap-1.5 text-uber-gray-400 text-sm">
          <Navigation className="w-3.5 h-3.5"/>
          <span>{ride.distanceKm.toFixed(1)} km</span>
        </div>
        {ride.driverId && (<div className="flex items-center gap-1.5 text-uber-gray-400 text-sm ml-auto">
            <span className="text-xs">🚗 {ride.driverId}</span>
          </div>)}
      </div>
    </div>);
}
