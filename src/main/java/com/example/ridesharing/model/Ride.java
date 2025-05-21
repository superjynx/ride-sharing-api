package com.example.ridesharing.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.example.ridesharing.enums.RideStatus;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "rides")
public class Ride {
    @Id
    private String id;

    private String driverUsername;

    @NotNull(message = "Origin is required")
    private String origin;

    @NotNull(message = "Destination is required")
    private String destination;

    @NotNull(message = "Departure time is required")
    private LocalDateTime departureTime;

    @Min(value = 1, message = "At least 1 seat must be available")
    private int availableSeats;

    @Min(value = 0, message = "Price cannot be negative")
    private double price;

    @NotNull(message = "Ride status is required")
    private RideStatus status = RideStatus.SCHEDULED;  // Default status

    // Campus-specific fields
    private String campusLocation;    // Specific campus location (e.g., "North Campus", "South Campus")
    private String buildingName;      // Specific building name
    private String scheduleType;      // "ONE_TIME" or "RECURRING"
    private String[] recurringDays;   // For recurring rides (e.g., ["MONDAY", "WEDNESDAY", "FRIDAY"])
    private String vehicleType;       // Type of vehicle
    private String vehicleNumber;     // Vehicle registration number
    private boolean isCarpool;        // Whether this is a carpool ride
    private String[] preferredDepartments; // Preferred departments for carpooling
    private String notes;             // Additional notes or requirements

    private int maxPassengers; // Maximum allowed passengers

    private Set<String> passengers = new HashSet<>();  // Store passenger usernames

    // Getters and setters

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getDriverUsername() {
        return driverUsername;
    }
    public void setDriverUsername(String driverUsername) {
        this.driverUsername = driverUsername;
    }

    public String getOrigin() {
        return origin;
    }
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }
    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }
    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }
    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }

    public RideStatus getStatus() {
        return status;
    }
    public void setStatus(RideStatus status) {
        this.status = status;
    }

    public String getCampusLocation() {
        return campusLocation;
    }
    public void setCampusLocation(String campusLocation) {
        this.campusLocation = campusLocation;
    }

    public String getBuildingName() {
        return buildingName;
    }
    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getScheduleType() {
        return scheduleType;
    }
    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public String[] getRecurringDays() {
        return recurringDays;
    }
    public void setRecurringDays(String[] recurringDays) {
        this.recurringDays = recurringDays;
    }

    public String getVehicleType() {
        return vehicleType;
    }
    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }
    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public boolean isCarpool() {
        return isCarpool;
    }
    public void setCarpool(boolean carpool) {
        isCarpool = carpool;
    }

    public String[] getPreferredDepartments() {
        return preferredDepartments;
    }
    public void setPreferredDepartments(String[] preferredDepartments) {
        this.preferredDepartments = preferredDepartments;
    }

    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getMaxPassengers() {
        return maxPassengers;
    }
    public void setMaxPassengers(int maxPassengers) {
        this.maxPassengers = maxPassengers;
    }

    public Set<String> getPassengers() {
        return passengers;
    }
    public void setPassengers(Set<String> passengers) {
        this.passengers = passengers;
    }
}
