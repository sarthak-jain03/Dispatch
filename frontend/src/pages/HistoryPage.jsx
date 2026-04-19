import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getUserRides } from '../api/rideApi';
import RideCard from '../components/RideCard';
import { Clock, Loader2, CarFront, AlertCircle } from 'lucide-react';
export default function HistoryPage() {
    const navigate = useNavigate();
    const [rides, setRides] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    useEffect(() => {
        const fetchRides = async () => {
            try {
                setLoading(true);
                const data = await getUserRides('user_101');
                // Sort by requestedAt descending
                data.sort((a, b) => new Date(b.requestedAt).getTime() - new Date(a.requestedAt).getTime());
                setRides(data);
            }
            catch (err) {
                setError(err?.response?.data?.message || err.message || 'Failed to load rides');
            }
            finally {
                setLoading(false);
            }
        };
        fetchRides();
    }, []);
    return (<div className="min-h-screen pt-16 bg-uber-dark">
      <div className="max-w-2xl mx-auto px-4 py-10">
        {/* Header */}
        <div className="flex items-center gap-3 mb-8">
          <div className="w-10 h-10 rounded-xl bg-uber-gray-800 flex items-center justify-center">
            <Clock className="w-5 h-5 text-uber-gray-300"/>
          </div>
          <div>
            <h1 className="text-2xl font-bold text-white">My Rides</h1>
            <p className="text-sm text-uber-gray-400">
              {rides.length} {rides.length === 1 ? 'ride' : 'rides'} total
            </p>
          </div>
        </div>

        {/* Loading */}
        {loading && (<div className="flex flex-col items-center justify-center py-20 gap-4">
            <Loader2 className="w-8 h-8 text-uber-green animate-spin"/>
            <p className="text-uber-gray-400 text-sm">Loading ride history...</p>
          </div>)}

        {/* Error */}
        {error && !loading && (<div className="card flex items-center gap-3 border-red-500/20 bg-red-500/5 mb-6">
            <AlertCircle className="w-5 h-5 text-red-400 shrink-0"/>
            <div>
              <p className="text-red-400 text-sm font-semibold">Error loading rides</p>
              <p className="text-uber-gray-400 text-xs mt-0.5">{error}</p>
            </div>
          </div>)}

        {/* Empty State */}
        {!loading && !error && rides.length === 0 && (<div className="text-center py-20">
            <div className="w-20 h-20 rounded-3xl bg-uber-gray-800 flex items-center justify-center mx-auto mb-6">
              <CarFront className="w-10 h-10 text-uber-gray-500"/>
            </div>
            <h3 className="text-xl font-bold text-white mb-2">No rides yet</h3>
            <p className="text-uber-gray-400 text-sm mb-8 max-w-sm mx-auto">
              Book your first ride and it will appear here.
            </p>
            <button onClick={() => navigate('/book')} className="btn-primary">
              Book a Ride
            </button>
          </div>)}

        {/* Ride List */}
        {!loading && rides.length > 0 && (<div className="space-y-4">
            {rides.map((ride) => (<RideCard key={ride.id} ride={ride} onClick={(r) => navigate(`/track/${r.id}`)}/>))}
          </div>)}
      </div>
    </div>);
}
