package com.location_service.service;


import com.location_service.dto.DriverLocationRequest;
import com.location_service.dto.NearbyDriver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoService {

    private static final String GEO_KEY = "drivers:locations";

    private final RedisTemplate<String, String>redisTemplate;

    public void updateDriverLocation(DriverLocationRequest request){
        GeoOperations<String, String>geoOps = redisTemplate.opsForGeo();

        Point point = new Point(request.getLongitude(), request.getLatitude());

        geoOps.add(GEO_KEY, point, request.getDriverId());
        log.debug("Updated location for {} → lat={}, lng={}", request.getDriverId(), request.getLatitude(), request.getLongitude());
    }



    public List<NearbyDriver>findNearbyDrivers(double pickupLat, double pickupLng, double radiusKm, int limit){
        GeoOperations<String, String>geoOps = redisTemplate.opsForGeo();

        Circle searchArea = new Circle(
                new Point(pickupLng, pickupLat), new Distance(radiusKm, Metrics.KILOMETERS)
        );

        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands
                .GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .includeDistance()
                .includeCoordinates()
                .sortAscending()
                .limit(limit);

        GeoResults<RedisGeoCommands.GeoLocation<String>>results = geoOps.radius(GEO_KEY, searchArea, args);

        List<NearbyDriver>nearbyDrivers = new ArrayList<>();

        if (results != null){
            for (GeoResult<RedisGeoCommands.GeoLocation<String>>result: results){
                String driverId = result.getContent().getName();
                double distance = result.getDistance().getValue();
                Point coordinates = result.getContent().getPoint();

                NearbyDriver driver = new NearbyDriver(
                        driverId,
                        distance,
                        coordinates.getY(),
                        coordinates.getX()
                );

                nearbyDrivers.add(driver);
            }
        }

        log.debug("Found {} drivers within {}km of ({}, {})",
                nearbyDrivers.size(), radiusKm, pickupLat, pickupLng);

        return nearbyDrivers;
    }

    public Point getDriverPosition(String driverId) {
        GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();

        List<Point> positions = geoOps.position(GEO_KEY, driverId);

        if (positions != null && !positions.isEmpty()) {
            return positions.get(0);
        }
        return null;
    }

    public double getDistance(String driverId1, String driverId2) {
        GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();

        Distance distance = geoOps.distance(GEO_KEY, driverId1, driverId2, Metrics.KILOMETERS);

        return distance != null ? distance.getValue() : -1;
    }

    public void removeDriver(String driverId){
        redisTemplate.opsForZSet().remove(GEO_KEY, driverId);
        log.debug("Removed driver {} from available pool", driverId);
    }

}
