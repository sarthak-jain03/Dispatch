package com.matching_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverFoundEvent {

    private String rideId;
    private String driverId;
    private String driverName;
    private double driverRating;

    private double driverLatitude;
    private double driverLongitude;

    private double distanceToPickupKm;
    private int estimatedArrivalMinutes;
}
