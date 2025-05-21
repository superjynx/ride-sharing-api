package com.example.ridesharing.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "notification_preferences")
public class NotificationPreference {
    @Id
    private String id;

    @jakarta.validation.constraints.NotBlank(message = "User ID is required")
    private String userId;
    private boolean rideStatusEnabled = true;
    private boolean bookingConfirmationEnabled = true;
    private boolean departureReminderEnabled = true;
    private boolean emailNotificationsEnabled = false;
    private boolean pushNotificationsEnabled = true;

    @jakarta.validation.constraints.Min(value = 0, message = "Reminder minutes must be zero or positive")
    private int reminderMinutesBefore = 30; // How many minutes before departure to send reminder

    public NotificationPreference() {
    }

    public NotificationPreference(String userId) {
        this.userId = userId;
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

    public boolean isRideStatusEnabled() {
        return rideStatusEnabled;
    }

    public void setRideStatusEnabled(boolean rideStatusEnabled) {
        this.rideStatusEnabled = rideStatusEnabled;
    }

    public boolean isBookingConfirmationEnabled() {
        return bookingConfirmationEnabled;
    }

    public void setBookingConfirmationEnabled(boolean bookingConfirmationEnabled) {
        this.bookingConfirmationEnabled = bookingConfirmationEnabled;
    }

    public boolean isDepartureReminderEnabled() {
        return departureReminderEnabled;
    }

    public void setDepartureReminderEnabled(boolean departureReminderEnabled) {
        this.departureReminderEnabled = departureReminderEnabled;
    }

    public boolean isEmailNotificationsEnabled() {
        return emailNotificationsEnabled;
    }

    public void setEmailNotificationsEnabled(boolean emailNotificationsEnabled) {
        this.emailNotificationsEnabled = emailNotificationsEnabled;
    }

    public boolean isPushNotificationsEnabled() {
        return pushNotificationsEnabled;
    }

    public void setPushNotificationsEnabled(boolean pushNotificationsEnabled) {
        this.pushNotificationsEnabled = pushNotificationsEnabled;
    }

    public int getReminderMinutesBefore() {
        return reminderMinutesBefore;
    }

    public void setReminderMinutesBefore(int reminderMinutesBefore) {
        this.reminderMinutesBefore = reminderMinutesBefore;
    }
}
