package com.location_service.controller;


import com.location_service.dto.DriverLocationRequest;
import com.location_service.dto.NearbyDriver;
import com.location_service.service.GeoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
@Slf4j
public class LocationController {

    private final GeoService geoService;


    @PostMapping("/update")
    public ResponseEntity<Map<String, String>>updateLocation(@RequestBody DriverLocationRequest request){
        log.info("Location update from driver: {}", request.getDriverId());
        geoService.updateDriverLocation(request);
        return ResponseEntity.ok(Map.of("message", "Location updated successfully"));
    }


    @GetMapping("/nearby")
    public ResponseEntity<List<NearbyDriver>>getNearbyDrivers(@RequestParam double lat, @RequestParam double lng,
                                                              @RequestParam(defaultValue = "5.0")double radius, @RequestParam(defaultValue = "10")int limit){
        log.info("Searching nearby drivers within {}km of ({}, {})", radius, lat, lng);
        List<NearbyDriver>drivers = geoService.findNearbyDrivers(lat, lng, radius, limit);
        return ResponseEntity.ok(drivers);
    }


    @GetMapping("/driver/{driverId}")
    public ResponseEntity<?>getDriverPosition(@PathVariable String driverId){
        Point point = geoService.getDriverPosition(driverId);

        if (point == null){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of(
                "driverId", driverId,
                "latitude", point.getY(),
                "longitude", point.getX()
        ));
    }


    @DeleteMapping("/driver/{driverId}")
    public ResponseEntity<Map<String, String>>removeDriver(@PathVariable String driverId) {
        geoService.removeDriver(driverId);
        return ResponseEntity.ok(Map.of("message", "Driver " + driverId + " removed successfully"));
    }
}
