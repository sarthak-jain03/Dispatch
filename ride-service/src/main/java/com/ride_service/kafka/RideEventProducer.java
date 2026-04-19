package com.ride_service.kafka;

import com.ride_service.dto.RideRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;


@Slf4j
@Component
@RequiredArgsConstructor
public class RideEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    public void publishRideRequested(RideRequestedEvent event) {

        log.info("Publishing ride.requested event for rideId: {}", event.getRideId());

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(
                        KafkaTopicConfig.RIDE_REQUESTED_TOPIC,
                        event.getRideId(),
                        event
                );

        future.whenComplete((result, ex) -> {
            if (ex != null) {

                log.error("Failed to publish ride.requested for rideId {}: {}",
                        event.getRideId(), ex.getMessage());
            }
            else{
                log.info("ride.requested published successfully → partition={}, offset={}",
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
