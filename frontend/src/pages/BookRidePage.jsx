import React, { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import MapView from '../components/MapView';
import RideBookingForm from '../components/RideBookingForm';
import DriverSimulator from '../components/DriverSimulator';
import { requestRide } from '../api/rideApi';
import { updateDriverLocation, getNearbyDrivers } from '../api/locationApi';
// Base fare constants mirroring the backend FareCalculator
const BASE_FARE = 30;
const PER_KM_RATE = 12;
const PER_MINUTE_RATE = 1.5;
const AVG_SPEED_KMH = 30;
const MIN_FARE = 40;
const EARTH_R = 6371;
function haversineKm(lat1, lng1, lat2, lng2) {
    const toRad = (d) => (d * Math.PI) / 180;
    const dLat = toRad(lat2 - lat1);
    const dLng = toRad(lng2 - lng1);
    const a = Math.sin(dLat / 2) ** 2 +
        Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng / 2) ** 2;
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return Math.round(EARTH_R * c * 100) / 100;
}
function estimateFare(distKm) {
    const mins = (distKm / AVG_SPEED_KMH) * 60;
    const fare = BASE_FARE + distKm * PER_KM_RATE + mins * PER_MINUTE_RATE;
    return Math.round(Math.max(fare, MIN_FARE) * 100) / 100;
}
export default function BookRidePage() {
    const navigate = useNavigate();
    const [pickupLat, setPickupLat] = useState(null);
    const [pickupLng, setPickupLng] = useState(null);
    const [dropLat, setDropLat] = useState(null);
    const [dropLng, setDropLng] = useState(null);
    const [selectMode, setSelectMode] = useState('pickup');
    const [loading, setLoading] = useState(false);
    const [nearbyDrivers, setNearbyDrivers] = useState([]);
    const [customDrivers, setCustomDrivers] = useState([]);
    // Calculated values
    const distance = pickupLat != null && pickupLng != null && dropLat != null && dropLng != null
        ? haversineKm(pickupLat, pickupLng, dropLat, dropLng)
        : null;
    const fare = distance != null ? estimateFare(distance) : null;
    // Handle map click
    const handleMapClick = useCallback(async (lat, lng) => {
        if (selectMode === 'pickup') {
            setPickupLat(lat);
            setPickupLng(lng);
            // Auto-switch to drop mode
            setSelectMode('drop');
        }
        else if (selectMode === 'drop') {
            setDropLat(lat);
            setDropLng(lng);
        }
        else if (selectMode === 'driver') {
            const driverId = `custom_d_${Math.floor(Math.random() * 1000)}`;
            try {
                // Add the new driver
                await updateDriverLocation({
                    driverId,
                    latitude: lat,
                    longitude: lng,
                });
                
                // Track custom driver for the sidebar
                setCustomDrivers(prev => [...prev, { driverId, latitude: lat, longitude: lng, label: 'Custom' }]);
                
                // Refresh nearby drivers if we already have pickup location
                const centerLat = pickupLat || lat;
                const centerLng = pickupLng || lng;
                const nearby = await getNearbyDrivers(centerLat, centerLng, 15, 15);
                setNearbyDrivers(nearby);
                
                // Return switch to pickup mode naturally
                setSelectMode('pickup');
            } catch (err) {
                console.error(err);
                alert("Failed to add custom driver");
            }
        }
    }, [selectMode, pickupLat, pickupLng]);
    // Handle booking
    const handleBook = async (pickupAddr, dropAddr) => {
        if (pickupLat == null || pickupLng == null || dropLat == null || dropLng == null)
            return;
        setLoading(true);
        try {
            const ride = await requestRide({
                userId: 'user_101',
                pickupLatitude: pickupLat,
                pickupLongitude: pickupLng,
                dropLatitude: dropLat,
                dropLongitude: dropLng,
                pickupAddress: pickupAddr,
                dropAddress: dropAddr,
            });
            // Navigate to tracking page
            navigate(`/track/${ride.id}`);
        }
        catch (err) {
            alert(err?.response?.data?.message || err.message || 'Failed to book ride. Make sure backend services are running.');
        }
        finally {
            setLoading(false);
        }
    };
    return (<div className="flex flex-col lg:flex-row h-screen pt-16">
      {/* ── Map ── */}
      <div className="flex-1 relative min-h-[300px] lg:min-h-0">
        <MapView pickup={pickupLat != null && pickupLng != null ? [pickupLat, pickupLng] : null} drop={dropLat != null && dropLng != null ? [dropLat, dropLng] : null} nearbyDrivers={nearbyDrivers} onMapClick={handleMapClick}/>

        {/* Map overlay hint */}
        <div className="absolute top-4 left-1/2 -translate-x-1/2 z-[1000] glass rounded-full px-4 py-2 pointer-events-none">
          <p className="text-xs text-uber-gray-200 font-medium">
            {selectMode === 'pickup' ? '📍 Click to set pickup' : 
             selectMode === 'drop' ? '🏁 Click to set drop-off' : 
             '🚗 Click anywhere to drop a new driver'}
          </p>
        </div>
      </div>

      {/* ── Sidebar ── */}
      <div className="w-full lg:w-[420px] bg-uber-dark border-l border-uber-gray-800 overflow-y-auto">
        <div className="p-6 space-y-8">
          {/* Booking Form */}
          <RideBookingForm pickupLat={pickupLat} pickupLng={pickupLng} dropLat={dropLat} dropLng={dropLng} onPickupChange={(lat, lng) => {
            setPickupLat(lat);
            setPickupLng(lng);
        }} onDropChange={(lat, lng) => {
            setDropLat(lat);
            setDropLng(lng);
        }} onBook={handleBook} loading={loading} estimatedFare={fare} estimatedDistance={distance} selectMode={selectMode} onSelectModeChange={setSelectMode}/>

          {/* Divider */}
          <div className="border-t border-uber-gray-800"/>

          {/* Driver Simulator */}
          <DriverSimulator onAddCustomDriver={() => setSelectMode('driver')} onDriversUpdated={(drivers) => setNearbyDrivers(drivers)} customDrivers={customDrivers}/>
        </div>
      </div>
    </div>);
}
