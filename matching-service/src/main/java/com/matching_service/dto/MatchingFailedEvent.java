package com.matching_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchingFailedEvent {
    private String rideId;
    private String reason;
    private LocalDateTime timestamp;
}
