import React, { useState, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import MapView from '../components/MapView';
import RideStatusTracker from '../components/RideStatusTracker';
import { useRidePolling } from '../hooks/useRidePolling';
import { markArrived, startRide, completeRide, cancelRide, } from '../api/rideApi';
import { getDriverPosition } from '../api/locationApi';
import { ArrowLeft, Loader2 } from 'lucide-react';
export default function TrackRidePage() {
    const { rideId } = useParams();
    const navigate = useNavigate();
    const { ride, loading, error, refetch } = useRidePolling({
        rideId: rideId || null,
        interval: 2000,
    });
    const [actionLoading, setActionLoading] = useState(false);
    const [driverLocation, setDriverLocation] = useState(null);
    // Fetch driver position when we have a driverId
    React.useEffect(() => {
        if (!ride?.driverId)
            return;
        let cancelled = false;
        const poll = async () => {
            const pos = await getDriverPosition(ride.driverId);
            if (!cancelled && pos) {
                setDriverLocation([pos.latitude, pos.longitude]);
            }
        };
        poll();
        const timer = setInterval(poll, 3000);
        return () => {
            cancelled = true;
            clearInterval(timer);
        };
    }, [ride?.driverId]);
    const handleAction = useCallback(async (action) => {
        setActionLoading(true);
        try {
            await action();
            refetch();
        }
        catch (err) {
            alert(err?.response?.data?.message || err.message || 'Action failed');
        }
        finally {
            setActionLoading(false);
        }
    }, [refetch]);
    // Loading state
    if (!ride && loading) {
        return (<div className="flex items-center justify-center h-screen pt-16">
        <div className="text-center space-y-4">
          <Loader2 className="w-10 h-10 text-uber-green animate-spin mx-auto"/>
          <p className="text-uber-gray-400 text-sm">Loading ride details...</p>
        </div>
      </div>);
    }
    // Error state
    if (error && !ride) {
        return (<div className="flex items-center justify-center h-screen pt-16">
        <div className="card text-center max-w-md mx-4 p-8">
          <p className="text-red-400 text-lg font-semibold mb-2">Ride not found</p>
          <p className="text-uber-gray-400 text-sm mb-6">{error}</p>
          <Link to="/book" className="btn-primary no-underline inline-flex">
            Book a new ride
          </Link>
        </div>
      </div>);
    }
    if (!ride)
        return null;
    const pickup = [ride.pickupLatitude, ride.pickupLongitude];
    const drop = [ride.dropLatitude, ride.dropLongitude];
    return (<div className="flex flex-col lg:flex-row h-screen pt-16">
      {/* ── Map ── */}
      <div className="flex-1 relative min-h-[300px] lg:min-h-0">
        <MapView pickup={pickup} drop={drop} driverLocation={driverLocation} center={pickup} zoom={14}/>

        {/* Back button overlay */}
        <button onClick={() => navigate('/book')} className="absolute top-4 left-4 z-[1000] glass rounded-xl p-2.5 cursor-pointer hover:bg-uber-gray-700 transition-colors">
          <ArrowLeft className="w-5 h-5 text-white"/>
        </button>
      </div>

      {/* ── Sidebar ── */}
      <div className="w-full lg:w-[420px] bg-uber-dark border-l border-uber-gray-800 overflow-y-auto">
        <div className="p-6">
          {/* Route Summary */}
          <div className="flex gap-3 mb-6 pb-6 border-b border-uber-gray-800">
            <div className="flex flex-col items-center pt-0.5">
              <div className="w-3 h-3 rounded-full bg-uber-green shrink-0"/>
              <div className="w-px flex-1 bg-uber-gray-600 my-1"/>
              <div className="w-3 h-3 rounded-sm bg-uber-blue shrink-0"/>
            </div>
            <div className="flex-1 space-y-4">
              <div>
                <p className="text-xs text-uber-gray-400 font-medium mb-0.5">PICKUP</p>
                <p className="text-white text-sm font-semibold">
                  {ride.pickupAddress || `${ride.pickupLatitude.toFixed(4)}, ${ride.pickupLongitude.toFixed(4)}`}
                </p>
              </div>
              <div>
                <p className="text-xs text-uber-gray-400 font-medium mb-0.5">DROP-OFF</p>
                <p className="text-white text-sm font-semibold">
                  {ride.dropAddress || `${ride.dropLatitude.toFixed(4)}, ${ride.dropLongitude.toFixed(4)}`}
                </p>
              </div>
            </div>
          </div>

          {/* Status Tracker */}
          <RideStatusTracker ride={ride} onDriverArrived={() => handleAction(() => markArrived(ride.id))} onStartRide={() => handleAction(() => startRide(ride.id))} onCompleteRide={() => handleAction(() => completeRide(ride.id))} onCancelRide={() => handleAction(() => cancelRide(ride.id))} actionLoading={actionLoading}/>

          {/* Post-completion */}
          {(ride.status === 'COMPLETED' || ride.status === 'CANCELLED') && (<div className="mt-8 pt-6 border-t border-uber-gray-800 space-y-3">
              <Link to="/book" className="btn-primary w-full no-underline">
                Book Another Ride
              </Link>
              <Link to="/history" className="btn-secondary w-full no-underline">
                View Ride History
              </Link>
            </div>)}
        </div>
      </div>
    </div>);
}
