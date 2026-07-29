package com.ride_service.kafka;

import com.ride_service.dto.MatchingFailedEvent;
import com.ride_service.saga.RideSagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchingFailedConsumer {

    private final RideSagaOrchestrator sagaOrchestrator;

    @KafkaListener(
            topics = KafkaTopicConfig.MATCHING_FAILED_TOPIC,
            groupId = "ride-service-group"
    )
    public void consumeMatchingFailed(MatchingFailedEvent event) {
        log.info("[Kafka] Received matching.failed event for rideId: {}, reason: {}", 
                event.getRideId(), event.getReason());
        try {
            sagaOrchestrator.compensateMatchingFailure(event.getRideId(), event.getReason());
        } catch (Exception e) {
            log.error("Failed to process matching.failed compensation for rideId {}: {}", 
                    event.getRideId(), e.getMessage(), e);
        }
    }
}
