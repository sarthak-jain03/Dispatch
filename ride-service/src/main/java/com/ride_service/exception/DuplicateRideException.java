package com.ride_service.exception;

public class DuplicateRideException extends RuntimeException {

    private final String existingRideId;

    public DuplicateRideException(String message, String existingRideId) {
        super(message);
        this.existingRideId = existingRideId;
    }

    public String getExistingRideId() {
        return existingRideId;
    }
}
