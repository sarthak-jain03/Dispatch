import React, { useState } from 'react';
import { MapPin, Navigation, DollarSign, Route, Loader2 } from 'lucide-react';
export default function RideBookingForm({ pickupLat, pickupLng, dropLat, dropLng, onPickupChange, onDropChange, onBook, loading = false, estimatedFare, estimatedDistance, selectMode, onSelectModeChange, }) {
    const [pickupAddress, setPickupAddress] = useState('');
    const [dropAddress, setDropAddress] = useState('');
    const canBook = pickupLat != null && pickupLng != null && dropLat != null && dropLng != null;
    const handleSubmit = (e) => {
        e.preventDefault();
        if (!canBook)
            return;
        onBook(pickupAddress || 'Pickup Location', dropAddress || 'Drop Location');
    };
    return (<form onSubmit={handleSubmit} className="flex flex-col gap-5">
      {/* Header */}
      <div>
        <h2 className="text-2xl font-bold text-white mb-1">Book a Ride</h2>
        <p className="text-uber-gray-400 text-sm">Click on the map to set locations</p>
      </div>

      {/* Mode Selector */}
      <div className="flex gap-2">
        <button type="button" onClick={() => onSelectModeChange('pickup')} className={`
            flex-1 flex items-center justify-center gap-2 py-3 rounded-xl text-sm font-semibold
            transition-all duration-200 border cursor-pointer
            ${selectMode === 'pickup'
            ? 'bg-uber-green/15 border-uber-green text-uber-green'
            : 'bg-uber-gray-800 border-uber-gray-600 text-uber-gray-300 hover:border-uber-gray-400'}
          `}>
          <MapPin className="w-4 h-4"/>
          Set Pickup
        </button>
        <button type="button" onClick={() => onSelectModeChange('drop')} className={`
            flex-1 flex items-center justify-center gap-2 py-3 rounded-xl text-sm font-semibold
            transition-all duration-200 border cursor-pointer
            ${selectMode === 'drop'
            ? 'bg-uber-blue/15 border-uber-blue text-uber-blue'
            : 'bg-uber-gray-800 border-uber-gray-600 text-uber-gray-300 hover:border-uber-gray-400'}
          `}>
          <Navigation className="w-4 h-4"/>
          Set Drop
        </button>
      </div>

      {/* Pickup Input */}
      <div className="space-y-2">
        <label className="flex items-center gap-2 text-xs font-semibold text-uber-gray-400 uppercase tracking-wider">
          <div className="w-2.5 h-2.5 rounded-full bg-uber-green"/>
          Pickup
        </label>
        <input type="text" placeholder="Pickup address (optional)" value={pickupAddress} onChange={(e) => setPickupAddress(e.target.value)} className="input-field"/>
        {pickupLat != null && pickupLng != null ? (<p className="text-xs text-uber-gray-400 font-mono">
            📍 {pickupLat.toFixed(4)}, {pickupLng.toFixed(4)}
          </p>) : (<p className="text-xs text-uber-gray-500 italic">Click map to set pickup</p>)}
      </div>

      {/* Vertical connector line */}
      <div className="flex items-center gap-3 -my-2 ml-[5px]">
        <div className="w-px h-6 bg-uber-gray-600 ml-[4px]"/>
      </div>

      {/* Drop Input */}
      <div className="space-y-2">
        <label className="flex items-center gap-2 text-xs font-semibold text-uber-gray-400 uppercase tracking-wider">
          <div className="w-2.5 h-2.5 rounded-sm bg-uber-blue"/>
          Drop-off
        </label>
        <input type="text" placeholder="Drop-off address (optional)" value={dropAddress} onChange={(e) => setDropAddress(e.target.value)} className="input-field"/>
        {dropLat != null && dropLng != null ? (<p className="text-xs text-uber-gray-400 font-mono">
            🏁 {dropLat.toFixed(4)}, {dropLng.toFixed(4)}
          </p>) : (<p className="text-xs text-uber-gray-500 italic">Click map to set drop-off</p>)}
      </div>

      {/* Fare Estimate */}
      {canBook && (<div className="bg-uber-gray-800 rounded-2xl p-4 border border-uber-gray-700 animate-[fade-in_0.3s_ease-out]">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-uber-green/10 flex items-center justify-center">
                <DollarSign className="w-5 h-5 text-uber-green"/>
              </div>
              <div>
                <p className="text-xs text-uber-gray-400 font-medium">Estimated Fare</p>
                <p className="text-xl font-bold text-white">
                  ₹{estimatedFare?.toFixed(0) || '—'}
                </p>
              </div>
            </div>
            <div className="text-right">
              <div className="flex items-center gap-1.5 text-uber-gray-400">
                <Route className="w-3.5 h-3.5"/>
                <span className="text-xs font-medium">{estimatedDistance?.toFixed(1) || '—'} km</span>
              </div>
            </div>
          </div>
        </div>)}

      {/* Book Button */}
      <button type="submit" disabled={!canBook || loading} className="btn-primary w-full text-base py-4 mt-1">
        {loading ? (<>
            <Loader2 className="w-5 h-5 animate-spin"/>
            Booking...
          </>) : ('Request Dispatch')}
      </button>
    </form>);
}
