import React from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polyline, useMapEvents, } from 'react-leaflet';
import L from 'leaflet';
// ── Custom Icon Factories ──
function createIcon(emoji, size = 32) {
    return L.divIcon({
        className: 'custom-marker',
        html: `<div style="
      font-size: ${size}px;
      line-height: 1;
      filter: drop-shadow(0 2px 4px rgba(0,0,0,0.5));
    ">${emoji}</div>`,
        iconSize: [size, size],
        iconAnchor: [size / 2, size],
        popupAnchor: [0, -size],
    });
}
const pickupIcon = createIcon('📍', 36);
const dropIcon = createIcon('🏁', 36);
const driverIcon = createIcon('🚗', 34);
const userIcon = createIcon('👤', 30);
// ── Default center (Bhopal, India) ──
const DEFAULT_CENTER = [23.2156, 77.4304];
function MapClickHandler({ onMapClick }) {
    useMapEvents({
        click(e) {
            onMapClick?.(e.latlng.lat, e.latlng.lng);
        },
    });
    return null;
}
export default function MapView({ pickup, drop, driverLocation, nearbyDrivers = [], onMapClick, center, zoom = 14, className = '', }) {
    const mapCenter = center || pickup || DEFAULT_CENTER;
    // Route line between pickup and drop
    const routeLine = [];
    if (pickup)
        routeLine.push(pickup);
    if (drop)
        routeLine.push(drop);
    return (<div className={`relative w-full h-full ${className}`}>
      <MapContainer center={mapCenter} zoom={zoom} className="w-full h-full" zoomControl={true} attributionControl={false}>
        <TileLayer url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png" attribution='&copy; <a href="https://carto.com/">CARTO</a>'/>

        {onMapClick && <MapClickHandler onMapClick={onMapClick}/>}

        {/* Pickup Marker */}
        {pickup && (<Marker position={pickup} icon={pickupIcon}>
            <Popup>
              <div className="text-center">
                <strong>Pickup Location</strong>
                <br />
                <span className="text-xs opacity-70">
                  {pickup[0].toFixed(4)}, {pickup[1].toFixed(4)}
                </span>
              </div>
            </Popup>
          </Marker>)}

        {/* Drop Marker */}
        {drop && (<Marker position={drop} icon={dropIcon}>
            <Popup>
              <div className="text-center">
                <strong>Drop Location</strong>
                <br />
                <span className="text-xs opacity-70">
                  {drop[0].toFixed(4)}, {drop[1].toFixed(4)}
                </span>
              </div>
            </Popup>
          </Marker>)}

        {/* Driver Location */}
        {driverLocation && (<Marker position={driverLocation} icon={driverIcon}>
            <Popup>
              <strong>Your Driver</strong>
            </Popup>
          </Marker>)}

        {/* Nearby Drivers */}
        {nearbyDrivers.map((d) => (<Marker key={d.driverId} position={[d.latitude, d.longitude]} icon={driverIcon}>
            <Popup>
              <strong>{d.driverId}</strong>
            </Popup>
          </Marker>))}

        {/* Route Line */}
        {routeLine.length === 2 && (<Polyline positions={routeLine} pathOptions={{
                color: '#FFFFFF',
                weight: 3,
                opacity: 0.6,
                dashArray: '8, 12',
            }}/>)}
      </MapContainer>
    </div>);
}
