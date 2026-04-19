package com.matching_service.service;

import com.matching_service.config.MatchingServiceConfig;
import com.matching_service.dto.DriverFoundEvent;
import com.matching_service.dto.NearbyDriver;
import com.matching_service.dto.RideRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingService {


    private final RestTemplate restTemplate;

    private final DriverScoringService driverScoringService;

    private final KafkaTemplate<String, Object> kafkaTemplate;


    @Value("${location-service.url}")
    private String locationServiceUrl;

    @Value("${matching.search-radius-km}")
    private double searchRadiusKm;

    @Value("${matching.max-drivers-to-consider}")
    private int maxDriversToConsider;


    public void matchDriverForRide(RideRequestedEvent event) {

        log.info("Starting driver match for rideId: {} at ({}, {})",
                event.getRideId(),
                event.getPickupLatitude(),
                event.getPickupLongitude());



        List<NearbyDriver> nearbyDrivers = fetchNearbyDrivers(
                event.getPickupLatitude(),
                event.getPickupLongitude()
        );

        if (nearbyDrivers == null || nearbyDrivers.isEmpty()) {
            log.warn("No drivers found within {}km for rideId: {}",
                    searchRadiusKm, event.getRideId());
            return;
        }

        log.info("Found {} candidate drivers", nearbyDrivers.size());

        List<NearbyDriver> rankedDrivers = driverScoringService.scoreAndRank(nearbyDrivers);
        NearbyDriver assignedDriver = tryAssignDriver(rankedDrivers, event);

        if (assignedDriver == null) {
            log.warn("All drivers declined for rideId: {}", event.getRideId());
            return;
        }

        log.info("Driver {} ({}) assigned to ride {}",
                assignedDriver.getDriverId(),
                assignedDriver.getName(),
                event.getRideId());

        publishDriverFound(event, assignedDriver);
    }


    private List<NearbyDriver> fetchNearbyDrivers(double lat, double lng) {

        String url = String.format(
                "%s/location/nearby?lat=%s&lng=%s&radius=%s&limit=%s",
                locationServiceUrl, lat, lng, searchRadiusKm, maxDriversToConsider
        );

        log.debug("Calling Location-Service: {}", url);

        try {
            // ParameterizedTypeReference lets us deserialize a generic List<NearbyDriver>
            // (RestTemplate can't figure out the generic type at runtime without this)
            ResponseEntity<List<NearbyDriver>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<NearbyDriver>>() {}
            );

            return response.getBody();

        } catch (Exception e) {
            log.error(" Failed to call Location-Service: {}", e.getMessage());
            return List.of();
        }
    }

    private NearbyDriver tryAssignDriver(List<NearbyDriver> rankedDrivers,
                                         RideRequestedEvent event) {
        for (NearbyDriver driver : rankedDrivers) {

            log.info("Notifying driver {} (score: {})...",
                    driver.getDriverId(), driver.getScore());


            boolean accepted = simulateDriverResponse(driver);

            if (accepted) {
                log.info("Driver {} accepted the ride!", driver.getDriverId());
                return driver;
            } else {
                log.info("Driver {} declined. Trying next...", driver.getDriverId());
            }
        }

        return null;
    }

    private boolean simulateDriverResponse(NearbyDriver driver) {


        double acceptanceProbability = 0.70;


        if (driver.getRating() >= 4.5) {
            acceptanceProbability += 0.10;
        }

        if (driver.getCancellationRate() > 0.10) {
            acceptanceProbability -= 0.20;
        }


        acceptanceProbability = Math.max(0.10, Math.min(0.95, acceptanceProbability));


        return Math.random() < acceptanceProbability;
    }

    private void publishDriverFound(RideRequestedEvent event, NearbyDriver assignedDriver) {
        int etaMinutes = (int) Math.ceil((assignedDriver.getDistanceKm() / 30.0) * 60);

        DriverFoundEvent driverFoundEvent = new DriverFoundEvent(
                event.getRideId(),
                assignedDriver.getDriverId(),
                assignedDriver.getName(),
                assignedDriver.getRating(),
                assignedDriver.getLatitude(),
                assignedDriver.getLongitude(),
                assignedDriver.getDistanceKm(),
                etaMinutes
        );

        log.info("Publishing driver.found → rideId: {}, driverId: {}, ETA: {}min",
                event.getRideId(), assignedDriver.getDriverId(), etaMinutes);

        kafkaTemplate.send(
                MatchingServiceConfig.DRIVER_FOUND_TOPIC,
                event.getRideId(),
                driverFoundEvent
        );
    }
}
