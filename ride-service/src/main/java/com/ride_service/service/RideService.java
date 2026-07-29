package com.ride_service.service;


import com.ride_service.dto.DriverFoundEvent;
import com.ride_service.dto.RideRequest;
import com.ride_service.dto.RideRequestedEvent;
import com.ride_service.entity.PaymentStatus;
import com.ride_service.entity.Ride;
import com.ride_service.entity.RideStatus;
import com.ride_service.entity.SagaStep;
import com.ride_service.exception.DuplicateRideException;
import com.ride_service.kafka.RideEventProducer;
import com.ride_service.repository.RideRepository;
import com.ride_service.saga.RideSagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;
    private final FareCalculator fareCalculator;
    private final RideEventProducer rideEventProducer;
    private final SagaEventService sagaEventService;
    private final IdempotencyService idempotencyService;
    private final RideSagaOrchestrator sagaOrchestrator;


    @Transactional
    public Ride requestRide(RideRequest request) {

        log.info("New ride request from user: {}", request.getUserId());

        // ── Step 1: Idempotency check ──
        String idempotencyKey = request.getIdempotencyKey();
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            idempotencyKey = IdempotencyService.generateKey(
                    request.getUserId(),
                    request.getPickupLatitude(), request.getPickupLongitude(),
                    request.getDropLatitude(), request.getDropLongitude()
            );
        }

        // Check Redis first (fast path)
        if (!idempotencyService.checkAndMark(idempotencyKey)) {
            // Redis says duplicate — check DB for the existing ride
            Optional<Ride> existing = rideRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                throw new DuplicateRideException(
                        "Duplicate ride request detected. Existing ride: " + existing.get().getId(),
                        existing.get().getId()
                );
            }
            // Redis had it but DB doesn't (edge case: previous ride was cleaned up) — allow
        }

        // Check DB-level unique constraint as fallback
        Optional<Ride> dbExisting = rideRepository.findByIdempotencyKey(idempotencyKey);
        if (dbExisting.isPresent()) {
            throw new DuplicateRideException(
                    "Duplicate ride request detected. Existing ride: " + dbExisting.get().getId(),
                    dbExisting.get().getId()
            );
        }

        // ── Step 2: Calculate fare ──
        double distanceKm = fareCalculator.calculateDistanceKm(
                request.getPickupLatitude(), request.getPickupLongitude(),
                request.getDropLatitude(),   request.getDropLongitude()
        );
        log.debug("Calculated ride distance: {} km", distanceKm);

        double estimatedFare = fareCalculator.calculateEstimatedFare(distanceKm);
        log.debug("Estimated fare: ₹{}", estimatedFare);

        // ── Step 3: Create ride ──
        Ride ride = new Ride();
        ride.setUserId(request.getUserId());
        ride.setPickupLatitude(request.getPickupLatitude());
        ride.setPickupLongitude(request.getPickupLongitude());
        ride.setDropLatitude(request.getDropLatitude());
        ride.setDropLongitude(request.getDropLongitude());
        ride.setPickupAddress(request.getPickupAddress());
        ride.setDropAddress(request.getDropAddress());
        ride.setDistanceKm(distanceKm);
        ride.setEstimatedFare(estimatedFare);
        ride.setStatus(RideStatus.REQUESTED);
        ride.setPaymentStatus(PaymentStatus.PENDING);
        ride.setIdempotencyKey(idempotencyKey);
        ride.setRequestedAt(LocalDateTime.now());

        Ride savedRide = rideRepository.save(ride);
        log.info("Ride saved to DB with ID: {}", savedRide.getId());

        // ── Step 4: Log saga event ──
        sagaEventService.recordStep(savedRide.getId(), SagaStep.RIDE_CREATED, "COMPLETED",
                "userId=" + request.getUserId() + ", fare=₹" + estimatedFare);

        // ── Step 5: Publish Kafka event ──
        RideRequestedEvent event = new RideRequestedEvent(
                savedRide.getId(),
                savedRide.getUserId(),
                savedRide.getPickupLatitude(),
                savedRide.getPickupLongitude(),
                savedRide.getDropLatitude(),
                savedRide.getDropLongitude(),
                savedRide.getEstimatedFare(),
                savedRide.getRequestedAt()
        );

        rideEventProducer.publishRideRequested(event);
        sagaEventService.recordStep(savedRide.getId(), SagaStep.MATCHING_REQUESTED, "STARTED",
                "Published ride.requested event");

        return savedRide;
    }

    @Transactional
    public void onDriverFound(DriverFoundEvent event) {

        log.info("Driver found for rideId: {} → driverId: {}",
                event.getRideId(), event.getDriverId());

        Ride ride = rideRepository.findById(event.getRideId())
                .orElseThrow(() -> new RuntimeException(
                        "Ride not found: " + event.getRideId()));

        // Verify ride is still in REQUESTED state (prevents double assignment)
        if (ride.getStatus() != RideStatus.REQUESTED) {
            log.warn("Ride {} already in status {} — ignoring driver.found event",
                    ride.getId(), ride.getStatus());
            return;
        }

        ride.setDriverId(event.getDriverId());
        ride.setStatus(RideStatus.DRIVER_ASSIGNED);

        rideRepository.save(ride);

        sagaEventService.recordStep(ride.getId(), SagaStep.DRIVER_ASSIGNED, "COMPLETED",
                "driverId=" + event.getDriverId());

        log.info("Ride {} updated to DRIVER_ASSIGNED with driver {}",
                ride.getId(), event.getDriverId());
    }


    @Transactional
    public Ride markDriverArrived(String rideId) {
        Ride ride = getRideOrThrow(rideId);

        if (ride.getStatus() != RideStatus.DRIVER_ASSIGNED) {
            throw new IllegalStateException("Cannot mark arrived — ride status is: " + ride.getStatus());
        }

        ride.setStatus(RideStatus.DRIVER_ARRIVED);
        Ride saved = rideRepository.save(ride);

        sagaEventService.recordStep(rideId, SagaStep.DRIVER_ARRIVED, "COMPLETED",
                "Driver arrived at pickup");

        return saved;
    }


    @Transactional
    public Ride startRide(String rideId) {
        Ride ride = getRideOrThrow(rideId);

        if (ride.getStatus() != RideStatus.DRIVER_ARRIVED) {
            throw new IllegalStateException("Cannot start ride — driver hasn't arrived yet");
        }

        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setStartedAt(LocalDateTime.now());
        Ride saved = rideRepository.save(ride);

        sagaEventService.recordStep(rideId, SagaStep.RIDE_STARTED, "COMPLETED",
                "Ride started");

        return saved;
    }


    @Transactional
    public Ride completeRide(String rideId) {
        Ride ride = getRideOrThrow(rideId);

        if (ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot complete — ride is not in progress");
        }

        LocalDateTime now = LocalDateTime.now();
        ride.setCompletedAt(now);

        long actualMinutes = java.time.Duration
                .between(ride.getStartedAt(), now)
                .toMinutes();

        double actualFare = fareCalculator.calculateActualFare(
                ride.getDistanceKm(), actualMinutes);

        ride.setActualFare(actualFare);

        // Move to PAYMENT_PENDING instead of COMPLETED
        ride.setStatus(RideStatus.PAYMENT_PENDING);
        ride.setPaymentStatus(PaymentStatus.PENDING);

        log.info("Ride {} completed. Distance: {}km, Duration: {}min, Fare: ₹{}",
                rideId, ride.getDistanceKm(), actualMinutes, actualFare);

        Ride saved = rideRepository.save(ride);

        sagaEventService.recordStep(rideId, SagaStep.RIDE_COMPLETED, "COMPLETED",
                "fare=₹" + actualFare + ", distance=" + ride.getDistanceKm() + "km");

        return saved;
    }


    @Transactional
    public Ride cancelRide(String rideId) {
        Ride ride = getRideOrThrow(rideId);

        if (ride.getStatus() == RideStatus.IN_PROGRESS
                || ride.getStatus() == RideStatus.COMPLETED
                || ride.getStatus() == RideStatus.PAYMENT_COMPLETED) {
            throw new IllegalStateException("Cannot cancel a ride that is already "
                    + ride.getStatus());
        }

        // Trigger saga compensation — release driver lock
        if (ride.getDriverId() != null) {
            sagaOrchestrator.compensateCancellation(rideId, ride.getDriverId());
        }

        ride.setStatus(RideStatus.CANCELLED);
        log.info("Ride {} cancelled", rideId);
        Ride saved = rideRepository.save(ride);

        sagaEventService.recordStep(rideId, SagaStep.RIDE_CANCELLED, "COMPLETED",
                "Ride cancelled by user");

        return saved;
    }


    public Ride getRide(String rideId) {
        return getRideOrThrow(rideId);
    }


    public List<Ride> getUserRides(String userId) {
        return rideRepository.findByUserId(userId);
    }


    private Ride getRideOrThrow(String rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found: " + rideId));
    }
}
