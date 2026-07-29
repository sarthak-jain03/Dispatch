package com.ride_service.repository;

import com.ride_service.entity.SagaEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SagaEventRepository extends JpaRepository<SagaEvent, String> {

    List<SagaEvent> findByRideIdOrderByTimestampAsc(String rideId);
}
