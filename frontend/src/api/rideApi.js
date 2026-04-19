import axios from 'axios';
const api = axios.create({
    baseURL: '/api/ride',
    headers: { 'Content-Type': 'application/json' },
});
/** User books a ride */
export async function requestRide(data) {
    const res = await api.post('/request', data);
    return res.data;
}
/** Get ride details by ID */
export async function getRide(rideId) {
    const res = await api.get(`/${rideId}`);
    return res.data;
}
/** Get all rides for a user */
export async function getUserRides(userId) {
    const res = await api.get(`/user/${userId}`);
    return res.data;
}
/** Driver marks arrived at pickup */
export async function markArrived(rideId) {
    const res = await api.put(`/${rideId}/arrived`);
    return res.data;
}
/** Driver starts the ride */
export async function startRide(rideId) {
    const res = await api.put(`/${rideId}/start`);
    return res.data;
}
/** Driver completes the ride */
export async function completeRide(rideId) {
    const res = await api.put(`/${rideId}/complete`);
    return res.data;
}
/** Cancel a ride */
export async function cancelRide(rideId) {
    const res = await api.put(`/${rideId}/cancel`);
    return res.data;
}
