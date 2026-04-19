package com.ride_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideRequest {

    private String userId;

    private double pickupLatitude;
    private double pickupLongitude;

    private double dropLatitude;
    private double dropLongitude;

    private String pickupAddress;
    private String dropAddress;
}
