package com.example.ridesharing.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;

@Document(collection = "bookings")
public class Booking {

    @Id
    private String id;

    @NotBlank(message = "Ride ID is required")
    private String rideId;      // The ride being booked

    @NotBlank(message = "Student ID is required")
    private String studentId;   // The student who booked the ride

    // constructors, getters, setters

    public Booking() {}

    public Booking(String rideId, String studentId) {
        this.rideId = rideId;
        this.studentId = studentId;
    }

    public String getId() {
        return id;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
}
