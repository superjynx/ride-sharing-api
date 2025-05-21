package com.example.ridesharing.enums;

public enum RideStatus {
    SCHEDULED,    // Initial state when ride is created
    IN_PROGRESS,  // Ride has started
    COMPLETED,    // Ride has finished
    CANCELLED     // Ride was cancelled
}
