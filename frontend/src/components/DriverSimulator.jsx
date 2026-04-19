import React, { useState, useEffect } from 'react';
import { Users, Loader2, CheckCircle2, AlertCircle } from 'lucide-react';
import { updateDriverLocation, getNearbyDrivers } from '../api/locationApi';
// Pre-configured demo drivers near Bhopal
const DEMO_DRIVERS = [
    { driverId: 'driver_01', latitude: 23.2165, longitude: 77.4315, label: 'Nearest' },
    { driverId: 'driver_02', latitude: 23.2200, longitude: 77.4350, label: 'Moderate' },
    { driverId: 'driver_03', latitude: 23.2250, longitude: 77.4400, label: 'Far' },
    { driverId: 'driver_04', latitude: 23.2100, longitude: 77.4250, label: 'South' },
    { driverId: 'driver_05', latitude: 23.2300, longitude: 77.4450, label: 'Furthest' },
];
export default function DriverSimulator({ onDriversUpdated, onAddCustomDriver, customDrivers = [], className = '' }) {
    const [registering, setRegistering] = useState(false);
    const [registered, setRegistered] = useState(new Set());
    
    // Auto-mark custom drivers as registered
    useEffect(() => {
        if (customDrivers.length > 0) {
            setRegistered(prev => {
                const next = new Set(prev);
                customDrivers.forEach(d => next.add(d.driverId));
                return next;
            });
        }
    }, [customDrivers]);
    
    const allDrivers = [...DEMO_DRIVERS, ...customDrivers];
    const [error, setError] = useState(null);
    const [nearbyDrivers, setNearbyDrivers] = useState([]);
    const registerAllDrivers = async () => {
        setRegistering(true);
        setError(null);
        try {
            for (const driver of DEMO_DRIVERS) {
                await updateDriverLocation({
                    driverId: driver.driverId,
                    latitude: driver.latitude,
                    longitude: driver.longitude,
                });
                setRegistered((prev) => new Set(prev).add(driver.driverId));
            }
            // Fetch nearby to confirm
            const nearby = await getNearbyDrivers(23.2156, 77.4304, 5, 10);
            setNearbyDrivers(nearby);
            onDriversUpdated?.(nearby);
        }
        catch (err) {
            setError(err?.message || 'Failed to register drivers');
        }
        finally {
            setRegistering(false);
        }
    };
    const registerSingleDriver = async (driver) => {
        try {
            await updateDriverLocation({
                driverId: driver.driverId,
                latitude: driver.latitude,
                longitude: driver.longitude,
            });
            setRegistered((prev) => new Set(prev).add(driver.driverId));
        }
        catch (err) {
            setError(err?.message || 'Failed');
        }
    };
    return (<div className={`space-y-4 ${className}`}>
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Users className="w-4 h-4 text-uber-green"/>
          <h3 className="text-sm font-bold text-white uppercase tracking-wider">Driver Simulator</h3>
        </div>
        <span className="text-xs text-uber-gray-500">{registered.size}/{allDrivers.length} online</span>
      </div>

      <p className="text-xs text-uber-gray-400">
        Register simulated drivers so the Matching Service can assign them to your ride.
      </p>

      {/* Register All */}
      <div className="flex gap-2">
        <button onClick={registerAllDrivers} disabled={registering || registered.size === allDrivers.length} className="btn-secondary flex-1">
          {registering ? (<>
              <Loader2 className="w-4 h-4 animate-spin"/>
              Registering...
            </>) : registered.size === allDrivers.length ? (<>
              <CheckCircle2 className="w-4 h-4 text-uber-green"/>
              All Online
            </>) : (<>
              <Users className="w-4 h-4"/>
              Register All
            </>)}
        </button>
        <button onClick={onAddCustomDriver} className="btn-secondary flex-1 text-xs px-2 whitespace-nowrap overflow-hidden text-ellipsis">
          📍 Add via Map
        </button>
      </div>

      {/* Driver List */}
      <div className="space-y-1.5 h-48 overflow-y-auto pr-1">
        {allDrivers.map((driver) => {
            const isRegistered = registered.has(driver.driverId);
            return (<div key={driver.driverId} className="flex items-center justify-between bg-uber-gray-800 rounded-xl px-3 py-2.5 border border-uber-gray-700">
              <div className="flex items-center gap-2.5">
                <div className={`w-2 h-2 rounded-full ${isRegistered ? 'bg-uber-green' : 'bg-uber-gray-600'}`}/>
                <div>
                  <p className="text-xs font-semibold text-white">{driver.driverId}</p>
                  <p className="text-[10px] text-uber-gray-500">{driver.label}</p>
                </div>
              </div>
              {!isRegistered && (<button onClick={() => registerSingleDriver(driver)} className="text-[11px] text-uber-gray-300 hover:text-white px-2.5 py-1 rounded-lg bg-uber-gray-700 hover:bg-uber-gray-600 transition-colors cursor-pointer">
                  Register
                </button>)}
              {isRegistered && (<CheckCircle2 className="w-4 h-4 text-uber-green"/>)}
            </div>);
        })}
      </div>

      {/* Error */}
      {error && (<div className="flex items-center gap-2 text-red-400 text-xs bg-red-500/10 rounded-xl px-3 py-2">
          <AlertCircle className="w-3.5 h-3.5 shrink-0"/>
          {error}
        </div>)}
    </div>);
}
