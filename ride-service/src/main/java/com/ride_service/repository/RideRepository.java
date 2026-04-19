package com.ride_service.repository;


import com.ride_service.entity.Ride;
import com.ride_service.entity.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RideRepository extends JpaRepository<Ride, String> {

    List<Ride>findByUserId(String userId);
    List<Ride>findByStatus(RideStatus status);

    Optional<Ride> findByUserIdAndStatus(String userId, RideStatus status);
}
