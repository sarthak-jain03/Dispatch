import axios from 'axios';
const api = axios.create({
    baseURL: '/api/location',
    headers: { 'Content-Type': 'application/json' },
});
/** Driver updates their GPS location */
export async function updateDriverLocation(data) {
    const res = await api.post('/update', data);
    return res.data;
}
/** Find nearby available drivers */
export async function getNearbyDrivers(lat, lng, radius = 5, limit = 10) {
    const res = await api.get('/nearby', {
        params: { lat, lng, radius, limit },
    });
    return res.data;
}
/** Get a specific driver's current position */
export async function getDriverPosition(driverId) {
    try {
        const res = await api.get(`/driver/${driverId}`);
        return res.data;
    }
    catch {
        return null;
    }
}
/** Remove driver from available pool */
export async function removeDriver(driverId) {
    const res = await api.delete(`/driver/${driverId}`);
    return res.data;
}
