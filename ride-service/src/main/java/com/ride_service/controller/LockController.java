package com.ride_service.controller;

import com.ride_service.service.DistributedLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/lock")
@RequiredArgsConstructor
public class LockController {

    private final DistributedLockService lockService;

    @PostMapping("/ride/{rideId}/acquire")
    public ResponseEntity<Void> acquireLock(@PathVariable String rideId, @RequestParam String driverId) {
        log.info("Attempting to acquire lock on ride {} for driver {}", rideId, driverId);
        boolean acquired = lockService.tryLock(rideId, driverId);
        if (acquired) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @DeleteMapping("/ride/{rideId}/release")
    public ResponseEntity<Void> releaseLock(@PathVariable String rideId, @RequestParam String driverId) {
        log.info("Releasing lock on ride {} for driver {}", rideId, driverId);
        boolean released = lockService.releaseLock(rideId, driverId);
        if (released) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
