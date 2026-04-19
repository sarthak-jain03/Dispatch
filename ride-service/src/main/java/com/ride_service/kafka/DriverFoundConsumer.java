package com.ride_service.kafka;

import com.ride_service.dto.DriverFoundEvent;
import com.ride_service.service.RideService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DriverFoundConsumer {

    private final RideService rideService;

    @KafkaListener(
            topics = KafkaTopicConfig.DRIVER_FOUND_TOPIC,
            groupId = "ride-service-group"
    )
    public void consumeDriverFound(DriverFoundEvent event) {

        log.info("[Kafka] Received driver.found event → rideId: {}, driverId: {}",
                event.getRideId(), event.getDriverId());

        try {
            rideService.onDriverFound(event);

            log.info("Successfully processed driver.found for rideId: {}",
                    event.getRideId());

        } catch (Exception e) {
            log.error("Failed to process driver.found event for rideId {}: {}",
                    event.getRideId(), e.getMessage(), e);
        }
    }
}
