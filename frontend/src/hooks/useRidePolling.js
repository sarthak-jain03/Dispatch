import { useEffect, useRef, useState, useCallback } from 'react';
import { getRide } from '../api/rideApi';
/**
 * Polls GET /ride/{rideId} at a regular interval.
 * Auto-stops when ride status is COMPLETED or CANCELLED.
 */
export function useRidePolling({ rideId, interval = 2000, enabled = true, }) {
    const [ride, setRide] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const timerRef = useRef(null);
    const fetchRide = useCallback(async () => {
        if (!rideId)
            return;
        try {
            setLoading(true);
            const data = await getRide(rideId);
            setRide(data);
            setError(null);
            // Stop polling on terminal states
            if (data.status === 'COMPLETED' || data.status === 'CANCELLED') {
                if (timerRef.current) {
                    clearInterval(timerRef.current);
                    timerRef.current = null;
                }
            }
        }
        catch (err) {
            setError(err?.response?.data?.message || err.message || 'Failed to fetch ride');
        }
        finally {
            setLoading(false);
        }
    }, [rideId]);
    useEffect(() => {
        if (!rideId || !enabled)
            return;
        // Initial fetch
        fetchRide();
        // Start polling
        timerRef.current = setInterval(fetchRide, interval);
        return () => {
            if (timerRef.current) {
                clearInterval(timerRef.current);
                timerRef.current = null;
            }
        };
    }, [rideId, interval, enabled, fetchRide]);
    return { ride, loading, error, refetch: fetchRide };
}
