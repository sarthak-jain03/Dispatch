package com.ride_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "saga_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SagaEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String rideId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SagaStep sagaStep;

    @Column(nullable = false)
    private String status; // STARTED, COMPLETED, FAILED, COMPENSATED

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
}
