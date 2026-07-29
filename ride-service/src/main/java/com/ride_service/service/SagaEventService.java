package com.ride_service.service;

import com.ride_service.entity.SagaEvent;
import com.ride_service.entity.SagaStep;
import com.ride_service.repository.SagaEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaEventService {

    private final SagaEventRepository sagaEventRepository;

    public void recordStep(String rideId, SagaStep step, String status, String payload) {
        SagaEvent event = new SagaEvent();
        event.setRideId(rideId);
        event.setSagaStep(step);
        event.setStatus(status);
        event.setPayload(payload);
        event.setTimestamp(LocalDateTime.now());

        sagaEventRepository.save(event);
        log.info("[SAGA] rideId={} step={} status={}", rideId, step, status);
    }

    public void recordFailure(String rideId, SagaStep step, String errorMessage) {
        SagaEvent event = new SagaEvent();
        event.setRideId(rideId);
        event.setSagaStep(step);
        event.setStatus("FAILED");
        event.setErrorMessage(errorMessage);
        event.setTimestamp(LocalDateTime.now());

        sagaEventRepository.save(event);
        log.error("[SAGA] rideId={} step={} FAILED: {}", rideId, step, errorMessage);
    }

    public void recordCompensation(String rideId, SagaStep step, String details) {
        SagaEvent event = new SagaEvent();
        event.setRideId(rideId);
        event.setSagaStep(step);
        event.setStatus("COMPENSATED");
        event.setPayload(details);
        event.setTimestamp(LocalDateTime.now());

        sagaEventRepository.save(event);
        log.warn("[SAGA] rideId={} step={} COMPENSATED: {}", rideId, step, details);
    }

    public List<SagaEvent> getEventLog(String rideId) {
        return sagaEventRepository.findByRideIdOrderByTimestampAsc(rideId);
    }
}
