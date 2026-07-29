package com.ride_service.saga;

import com.ride_service.entity.Ride;
import com.ride_service.entity.RideStatus;
import com.ride_service.entity.SagaStep;
import com.ride_service.repository.RideRepository;
import com.ride_service.service.DistributedLockService;
import com.ride_service.service.PaymentService;
import com.ride_service.service.SagaEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Saga Orchestrator for the ride lifecycle.
 *
 * Defines compensation chains for each saga step:
 * - MATCHING_REQUESTED fails → mark ride MATCHING_FAILED
 * - DRIVER_ASSIGNED fails → release lock, re-add driver to pool, mark CANCELLED
 * - PAYMENT_ORDER_CREATED fails → mark ride PAYMENT_FAILED
 * - PAYMENT_VERIFIED fails → initiate refund
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RideSagaOrchestrator {

    private final RideRepository rideRepository;
    private final SagaEventService sagaEventService;
    private final DistributedLockService lockService;
    private final PaymentService paymentService;

    /**
     * Compensate for matching failure — no drivers found or all declined.
     */
    @Transactional
    public void compensateMatchingFailure(String rideId, String reason) {
        log.warn("[SAGA COMPENSATION] Matching failed for ride {}: {}", rideId, reason);

        Ride ride = rideRepository.findById(rideId).orElse(null);
        if (ride == null) {
            log.error("[SAGA COMPENSATION] Ride {} not found for compensation", rideId);
            return;
        }

        // Only compensate if ride is still in REQUESTED state
        if (ride.getStatus() != RideStatus.REQUESTED) {
            log.info("[SAGA COMPENSATION] Ride {} already moved to {} — skipping", rideId, ride.getStatus());
            return;
        }

        ride.setStatus(RideStatus.MATCHING_FAILED);
        rideRepository.save(ride);

        sagaEventService.recordCompensation(rideId, SagaStep.MATCHING_REQUESTED,
                "Matching failed: " + reason);
    }

    /**
     * Compensate for driver assignment failure — release lock, revert status.
     */
    @Transactional
    public void compensateDriverAssignment(String rideId, String driverId) {
        log.warn("[SAGA COMPENSATION] Driver assignment failed for ride {}", rideId);

        Ride ride = rideRepository.findById(rideId).orElse(null);
        if (ride == null) {
            log.error("[SAGA COMPENSATION] Ride {} not found for compensation", rideId);
            return;
        }

        // Release the distributed lock
        if (driverId != null) {
            lockService.releaseLock(rideId, driverId);
        }

        // Revert to REQUESTED so matching can retry, or CANCELLED
        ride.setDriverId(null);
        ride.setStatus(RideStatus.CANCELLED);
        rideRepository.save(ride);

        sagaEventService.recordCompensation(rideId, SagaStep.DRIVER_ASSIGNED,
                "Driver assignment reverted, lock released for driver=" + driverId);
    }

    /**
     * Compensate for ride cancellation after driver was assigned.
     * Releases the lock so the driver becomes available again.
     */
    @Transactional
    public void compensateCancellation(String rideId, String driverId) {
        log.warn("[SAGA COMPENSATION] Ride {} cancelled, releasing driver {}", rideId, driverId);

        // Release the distributed lock
        if (driverId != null) {
            lockService.releaseLock(rideId, driverId);
        }

        sagaEventService.recordCompensation(rideId, SagaStep.RIDE_CANCELLED,
                "Ride cancelled, driver lock released for driver=" + driverId);
    }

    /**
     * Compensate for payment failure — mark ride as payment failed.
     */
    @Transactional
    public void compensatePaymentFailure(String rideId, String reason) {
        log.warn("[SAGA COMPENSATION] Payment failed for ride {}: {}", rideId, reason);

        Ride ride = rideRepository.findById(rideId).orElse(null);
        if (ride == null) return;

        ride.setStatus(RideStatus.PAYMENT_FAILED);
        rideRepository.save(ride);

        sagaEventService.recordCompensation(rideId, SagaStep.PAYMENT_ORDER_CREATED,
                "Payment failed: " + reason);
    }

    /**
     * Compensate for payment verification failure — initiate refund if payment was captured.
     */
    @Transactional
    public void compensatePaymentVerification(String rideId) {
        log.warn("[SAGA COMPENSATION] Payment verification failed for ride {} — initiating refund", rideId);

        try {
            paymentService.initiateRefund(rideId);
        } catch (Exception e) {
            log.error("[SAGA COMPENSATION] Refund failed for ride {}: {}", rideId, e.getMessage());
            sagaEventService.recordFailure(rideId, SagaStep.PAYMENT_VERIFIED,
                    "Refund compensation failed: " + e.getMessage());
        }
    }
}
