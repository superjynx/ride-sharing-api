package com.example.ridesharing.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "notifications")
public class Notification {
    @Id
    private String id;
    private String userId;
    private String title;
    private String message;
    private LocalDateTime timestamp;
    private boolean read;
    private String rideId;  // Optional: reference to related ride
    private NotificationType type;

    public enum NotificationType {
        RIDE_STATUS_CHANGE,
        RIDE_BOOKED,
        RIDE_CANCELLED,
        RIDE_REMINDER,
        BOOKING_CONFIRMED
    }

    // Constructor
    public Notification(String userId, String title, String message, NotificationType type, String rideId) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.read = false;
        this.type = type;
        this.rideId = rideId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }
}
