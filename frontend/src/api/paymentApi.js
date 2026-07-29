import axios from 'axios';

const api = axios.create({
    baseURL: '/api/payment',
    headers: { 'Content-Type': 'application/json' },
});

export async function createPaymentOrder(rideId) {
    const res = await api.post('/create-order', { rideId });
    return res.data;
}

export async function verifyPayment(data) {
    const res = await api.post('/verify', data);
    return res.data;
}

export async function getPaymentStatus(rideId) {
    const res = await api.get(`/${rideId}/status`);
    return res.data;
}
