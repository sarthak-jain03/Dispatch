package com.ride_service.dto;

import com.ride_service.entity.Ride;
import com.ride_service.entity.RideStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideResponse {

    private String rideId;
    private String userId;
    private String driverId;
    private String driverName;

    private String pickupAddress;
    private String dropAddress;

    private double pickupLatitude;
    private double pickupLongitude;
    private double dropLatitude;
    private double dropLongitude;

    private double distanceKm;
    private double estimatedFare;
    private double actualFare;

    private RideStatus status;
    private String statusMessage;

    private LocalDateTime requestedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;


    public static RideResponse from(Ride ride) {
        RideResponse response = new RideResponse();

        response.setRideId(ride.getId());
        response.setUserId(ride.getUserId());
        response.setDriverId(ride.getDriverId());

        response.setPickupAddress(ride.getPickupAddress());
        response.setDropAddress(ride.getDropAddress());

        response.setPickupLatitude(ride.getPickupLatitude());
        response.setPickupLongitude(ride.getPickupLongitude());
        response.setDropLatitude(ride.getDropLatitude());
        response.setDropLongitude(ride.getDropLongitude());

        response.setDistanceKm(ride.getDistanceKm());
        response.setEstimatedFare(ride.getEstimatedFare());
        response.setActualFare(ride.getActualFare());

        response.setStatus(ride.getStatus());

        response.setRequestedAt(ride.getRequestedAt());
        response.setStartedAt(ride.getStartedAt());
        response.setCompletedAt(ride.getCompletedAt());

        return response;
    }
}