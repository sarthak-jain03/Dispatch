package com.ride_service.service;


import com.ride_service.dto.DriverFoundEvent;
import com.ride_service.dto.RideRequest;
import com.ride_service.dto.RideRequestedEvent;
import com.ride_service.entity.Ride;
import com.ride_service.entity.RideStatus;
import com.ride_service.kafka.RideEventProducer;
import com.ride_service.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;
    private final FareCalculator fareCalculator;
    private final RideEventProducer rideEventProducer;


    @Transactional
    public Ride requestRide(RideRequest request) {

        log.info("New ride request from user: {}", request.getUserId());


        double distanceKm = fareCalculator.calculateDistanceKm(
                request.getPickupLatitude(), request.getPickupLongitude(),
                request.getDropLatitude(),   request.getDropLongitude()
        );
        log.debug("Calculated ride distance: {} km", distanceKm);


        double estimatedFare = fareCalculator.calculateEstimatedFare(distanceKm);
        log.debug("Estimated fare: ₹{}", estimatedFare);

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
        ride.setRequestedAt(LocalDateTime.now());

        Ride savedRide = rideRepository.save(ride);
        log.info("Ride saved to DB with ID: {}", savedRide.getId());


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

        return savedRide;
    }

    @Transactional
    public void onDriverFound(DriverFoundEvent event) {

        log.info("Driver found for rideId: {} → driverId: {}",
                event.getRideId(), event.getDriverId());

        Ride ride = rideRepository.findById(event.getRideId())
                .orElseThrow(() -> new RuntimeException(
                        "Ride not found: " + event.getRideId()));


        ride.setDriverId(event.getDriverId());
        ride.setStatus(RideStatus.DRIVER_ASSIGNED);

        rideRepository.save(ride);

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
        return rideRepository.save(ride);
    }


    @Transactional
    public Ride startRide(String rideId) {
        Ride ride = getRideOrThrow(rideId);

        if (ride.getStatus() != RideStatus.DRIVER_ARRIVED) {
            throw new IllegalStateException("Cannot start ride — driver hasn't arrived yet");
        }

        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setStartedAt(LocalDateTime.now());
        return rideRepository.save(ride);
    }


    @Transactional
    public Ride completeRide(String rideId) {
        Ride ride = getRideOrThrow(rideId);

        if (ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot complete — ride is not in progress");
        }

        LocalDateTime now = LocalDateTime.now();
        ride.setCompletedAt(now);
        ride.setStatus(RideStatus.COMPLETED);

        long actualMinutes = java.time.Duration
                .between(ride.getStartedAt(), now)
                .toMinutes();


        double actualFare = fareCalculator.calculateActualFare(
                ride.getDistanceKm(), actualMinutes);

        ride.setActualFare(actualFare);

        log.info("Ride {} completed. Distance: {}km, Duration: {}min, Fare: ₹{}",
                rideId, ride.getDistanceKm(), actualMinutes, actualFare);

        return rideRepository.save(ride);
    }


    @Transactional
    public Ride cancelRide(String rideId) {
        Ride ride = getRideOrThrow(rideId);

        if (ride.getStatus() == RideStatus.IN_PROGRESS
                || ride.getStatus() == RideStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a ride that is already "
                    + ride.getStatus());
        }

        ride.setStatus(RideStatus.CANCELLED);
        log.info("Ride {} cancelled", rideId);
        return rideRepository.save(ride);
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
