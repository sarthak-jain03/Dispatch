package com.matching_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/matching")
@RequiredArgsConstructor
public class MatchingController {


    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "service",     "matching-service",
                "status",      "UP",
                "port",        "8083",
                "description", "Listening to Kafka topic: ride.requested"
        ));
    }


    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> config(
            @org.springframework.beans.factory.annotation.Value("${matching.search-radius-km}") double radius,
            @org.springframework.beans.factory.annotation.Value("${matching.max-drivers-to-consider}") int maxDrivers,
            @org.springframework.beans.factory.annotation.Value("${matching.driver-timeout-seconds}") int timeout
    ) {
        return ResponseEntity.ok(Map.of(
                "searchRadiusKm",        radius,
                "maxDriversToConsider",  maxDrivers,
                "driverTimeoutSeconds",  timeout,
                "scoringWeights", Map.of(
                        "distance",    "40%",
                        "rating",      "35%",
                        "experience",  "15%",
                        "reliability", "10%"
                )
        ));
    }
}
