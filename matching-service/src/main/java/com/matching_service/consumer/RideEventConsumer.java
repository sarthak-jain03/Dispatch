package com.matching_service.consumer;


import com.matching_service.dto.RideRequestedEvent;
import com.matching_service.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RideEventConsumer {

    private final MatchingService matchingService;

    @KafkaListener(
            topics = "ride.requested",
            groupId = "matching-service-group"
    )
    public void onRideRequested(RideRequestedEvent event) {

        log.info("[Kafka] Received ride.requested → rideId: {}, userId: {}",
                event.getRideId(), event.getUserId());

        try {
            matchingService.matchDriverForRide(event);

        } catch (Exception e) {
            log.error("Error processing ride.requested for rideId {}: {}",
                    event.getRideId(), e.getMessage(), e);
        }
    }
}
