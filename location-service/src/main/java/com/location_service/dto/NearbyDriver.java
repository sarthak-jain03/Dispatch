package com.location_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NearbyDriver {

    private String driverId;
    private double distanceKm;
    private double latitude;
    private double longitude;
}
