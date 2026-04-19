package com.matching_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideRequestedEvent {

    private String rideId;
    private String userId;

    private double pickupLatitude;
    private double pickupLongitude;

    private double dropLatitude;
    private double dropLongitude;

    private double estimatedFare;
    private LocalDateTime requestedAt;
}
