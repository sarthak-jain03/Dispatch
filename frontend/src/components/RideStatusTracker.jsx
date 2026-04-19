import React from 'react';
import StatusBadge from './StatusBadge';
import { Search, UserCheck, MapPin, Play, CheckCircle2, XCircle, Car, Clock, DollarSign, } from 'lucide-react';
// ── Lifecycle steps ──
const LIFECYCLE_STEPS = [
    { status: 'REQUESTED', label: 'Searching Driver', icon: Search },
    { status: 'DRIVER_ASSIGNED', label: 'Driver Assigned', icon: UserCheck },
    { status: 'DRIVER_ARRIVED', label: 'Driver Arrived', icon: MapPin },
    { status: 'IN_PROGRESS', label: 'On the Way', icon: Play },
    { status: 'COMPLETED', label: 'Completed', icon: CheckCircle2 },
];
const STATUS_ORDER = [
    'REQUESTED',
    'DRIVER_ASSIGNED',
    'DRIVER_ARRIVED',
    'IN_PROGRESS',
    'COMPLETED',
];
export default function RideStatusTracker({ ride, onDriverArrived, onStartRide, onCompleteRide, onCancelRide, actionLoading = false, }) {
    const currentIndex = STATUS_ORDER.indexOf(ride.status);
    const isCancelled = ride.status === 'CANCELLED';
    return (<div className="flex flex-col gap-6">
      {/* Status Header */}
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-xl font-bold text-white mb-1">Ride Status</h3>
          <p className="text-sm text-uber-gray-400 font-mono">
            ID: {ride.id.slice(0, 8)}...
          </p>
        </div>
        <StatusBadge status={ride.status} size="md" pulse/>
      </div>

      {/* Cancelled State */}
      {isCancelled && (<div className="flex items-center gap-3 bg-red-500/10 border border-red-500/20 rounded-2xl p-4">
          <XCircle className="w-6 h-6 text-red-400 shrink-0"/>
          <div>
            <p className="text-red-400 font-semibold">Ride Cancelled</p>
            <p className="text-sm text-uber-gray-400">This ride has been cancelled.</p>
          </div>
        </div>)}

      {/* Progress Steps */}
      {!isCancelled && (<div className="space-y-0">
          {LIFECYCLE_STEPS.map((step, idx) => {
                const isCompleted = currentIndex > idx;
                const isActive = currentIndex === idx;
                const Icon = step.icon;
                return (<div key={step.status} className="relative flex gap-4">
                {/* Vertical line */}
                {idx < LIFECYCLE_STEPS.length - 1 && (<div className={`absolute left-[17px] top-[36px] w-0.5 h-[calc(100%-4px)] ${isCompleted ? 'bg-uber-green' : 'bg-uber-gray-700'}`}/>)}

                {/* Circle */}
                <div className={`
                    relative z-10 shrink-0 w-[36px] h-[36px] rounded-full flex items-center justify-center
                    transition-all duration-300
                    ${isCompleted
                        ? 'bg-uber-green text-white'
                        : isActive
                            ? 'bg-white text-black ring-4 ring-white/20'
                            : 'bg-uber-gray-800 text-uber-gray-500 border border-uber-gray-700'}
                  `}>
                  <Icon className="w-4 h-4"/>
                </div>

                {/* Label */}
                <div className="pb-8 pt-1.5">
                  <p className={`text-sm font-semibold ${isCompleted
                        ? 'text-uber-green'
                        : isActive
                            ? 'text-white'
                            : 'text-uber-gray-500'}`}>
                    {step.label}
                  </p>
                  {isActive && step.status === 'REQUESTED' && (<p className="text-xs text-uber-gray-400 mt-0.5">Looking for nearby drivers...</p>)}
                  {isActive && step.status === 'DRIVER_ASSIGNED' && ride.driverId && (<p className="text-xs text-uber-gray-400 mt-0.5">
                      Driver <span className="text-uber-blue font-medium">{ride.driverId}</span> is on the way
                    </p>)}
                </div>
              </div>);
            })}
        </div>)}

      {/* Driver Info Card */}
      {ride.driverId && !isCancelled && (<div className="bg-uber-gray-800 rounded-2xl p-4 border border-uber-gray-700 animate-[fade-in_0.3s_ease-out]">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-full bg-gradient-to-br from-uber-green to-uber-blue flex items-center justify-center">
              <Car className="w-6 h-6 text-white"/>
            </div>
            <div>
              <p className="text-white font-bold text-base">{ride.driverId}</p>
              <p className="text-uber-gray-400 text-xs">Your driver</p>
            </div>
          </div>
        </div>)}

      {/* Ride Details */}
      <div className="grid grid-cols-2 gap-3">
        <div className="bg-uber-gray-800 rounded-xl p-3.5 border border-uber-gray-700">
          <div className="flex items-center gap-2 mb-1">
            <DollarSign className="w-3.5 h-3.5 text-uber-green"/>
            <span className="text-xs text-uber-gray-400">Fare</span>
          </div>
          <p className="text-lg font-bold text-white">
            ₹{(ride.actualFare || ride.estimatedFare).toFixed(0)}
          </p>
        </div>
        <div className="bg-uber-gray-800 rounded-xl p-3.5 border border-uber-gray-700">
          <div className="flex items-center gap-2 mb-1">
            <Clock className="w-3.5 h-3.5 text-uber-blue"/>
            <span className="text-xs text-uber-gray-400">Distance</span>
          </div>
          <p className="text-lg font-bold text-white">
            {ride.distanceKm.toFixed(1)} km
          </p>
        </div>
      </div>

      {/* Action Buttons (for demo: simulate driver actions) */}
      {!isCancelled && ride.status !== 'COMPLETED' && (<div className="space-y-2 pt-2 border-t border-uber-gray-800">
          <p className="text-xs text-uber-gray-500 uppercase tracking-wider font-semibold mb-3">
            Demo Controls
          </p>

          {ride.status === 'DRIVER_ASSIGNED' && (<button onClick={onDriverArrived} disabled={actionLoading} className="btn-secondary w-full">
              📍 Mark Driver Arrived
            </button>)}

          {ride.status === 'DRIVER_ARRIVED' && (<button onClick={onStartRide} disabled={actionLoading} className="btn-green w-full">
              ▶️ Start Ride
            </button>)}

          {ride.status === 'IN_PROGRESS' && (<button onClick={onCompleteRide} disabled={actionLoading} className="btn-green w-full">
              🏁 Complete Ride
            </button>)}

          {(ride.status === 'REQUESTED' || ride.status === 'DRIVER_ASSIGNED') && (<button onClick={onCancelRide} disabled={actionLoading} className="btn-danger w-full mt-2">
              Cancel Ride
            </button>)}
        </div>)}
    </div>);
}
