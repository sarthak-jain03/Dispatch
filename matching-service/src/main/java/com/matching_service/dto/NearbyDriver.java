package com.matching_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NearbyDriver {

    private String driverId;

    private Double distanceKm;
    private Double latitude;
    private Double longitude;

    private String name;
    private Double rating;
    private Integer tripsCompleted;
    private Double cancellationRate;

    private Double score;
}