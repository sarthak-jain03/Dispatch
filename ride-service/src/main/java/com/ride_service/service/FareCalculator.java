package com.ride_service.service;

import org.springframework.stereotype.Component;

@Component
public class FareCalculator {


    private static final double EARTH_RADIUS_KM = 6371.0;


    private static final double BASE_FARE        = 30.0;
    private static final double PER_KM_RATE      = 12.0;
    private static final double PER_MINUTE_RATE  = 1.5;
    private static final double MINIMUM_FARE     = 40.0;


    private static final double AVG_SPEED_KMH = 30.0;

    public double calculateDistanceKm(double lat1, double lng1,
                                      double lat2, double lng2) {

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        // Haversine formula
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));


        double distanceKm = EARTH_RADIUS_KM * c;

        return Math.round(distanceKm * 100.0) / 100.0;
    }


    public double calculateEstimatedFare(double distanceKm) {

        double estimatedMinutes = (distanceKm / AVG_SPEED_KMH) * 60;

        double fare = BASE_FARE + (distanceKm * PER_KM_RATE + (estimatedMinutes * PER_MINUTE_RATE));


        fare = Math.max(fare, MINIMUM_FARE);
        return Math.round(fare * 100.0) / 100.0;
    }


    public double calculateActualFare(double actualDistanceKm, double actualMinutes) {
        double fare = BASE_FARE + (actualDistanceKm * PER_KM_RATE) + (actualMinutes * PER_MINUTE_RATE);

        fare = Math.max(fare, MINIMUM_FARE);
        return Math.round(fare * 100.0) / 100.0;
    }


    public int estimateArrivalMinutes(double distanceKm) {
        double hours = distanceKm / AVG_SPEED_KMH;
        return (int) Math.ceil(hours * 60);
    }
}
