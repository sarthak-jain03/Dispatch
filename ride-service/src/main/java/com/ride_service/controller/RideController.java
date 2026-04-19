package com.ride_service.controller;

import com.ride_service.dto.RideRequest;
import com.ride_service.entity.Ride;
import com.ride_service.service.RideService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ride")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;


    @PostMapping("/request")
    public ResponseEntity<Ride> requestRide(@RequestBody RideRequest request) {
        log.info("POST /ride/request from userId: {}", request.getUserId());
        Ride ride = rideService.requestRide(request);
        return ResponseEntity.ok(ride);
    }


    @GetMapping("/{rideId}")
    public ResponseEntity<Ride> getRide(@PathVariable String rideId) {
        Ride ride = rideService.getRide(rideId);
        return ResponseEntity.ok(ride);
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Ride>> getUserRides(@PathVariable String userId) {
        List<Ride> rides = rideService.getUserRides(userId);
        return ResponseEntity.ok(rides);
    }


    @PutMapping("/{rideId}/arrived")
    public ResponseEntity<Ride> driverArrived(@PathVariable String rideId) {
        log.info("Driver arrived for rideId: {}", rideId);
        Ride ride = rideService.markDriverArrived(rideId);
        return ResponseEntity.ok(ride);
    }

    @PutMapping("/{rideId}/start")
    public ResponseEntity<Ride> startRide(@PathVariable String rideId) {
        log.info("▶Ride started: {}", rideId);
        Ride ride = rideService.startRide(rideId);
        return ResponseEntity.ok(ride);
    }

    @PutMapping("/{rideId}/complete")
    public ResponseEntity<Ride> completeRide(@PathVariable String rideId) {
        log.info("Ride completed: {}", rideId);
        Ride ride = rideService.completeRide(rideId);
        return ResponseEntity.ok(ride);
    }


    @PutMapping("/{rideId}/cancel")
    public ResponseEntity<Ride> cancelRide(@PathVariable String rideId) {
        log.info("Ride cancelled: {}", rideId);
        Ride ride = rideService.cancelRide(rideId);
        return ResponseEntity.ok(ride);
    }


    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "service", "ride-service",
                "status", "UP",
                "port", "8082"
        ));
    }
}
