package com.example.ridesharing.service;

import com.example.ridesharing.exception.ResourceNotFoundException;
import com.example.ridesharing.exception.BadRequestException;
import com.example.ridesharing.model.NotificationPreference;
import com.example.ridesharing.repository.NotificationPreferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationPreferenceService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationPreferenceService.class);
    private final NotificationPreferenceRepository preferenceRepository;

    public NotificationPreferenceService(NotificationPreferenceRepository preferenceRepository) {
        this.preferenceRepository = preferenceRepository;
    }

    public NotificationPreference getPreferences(String userId) {
        return preferenceRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification preferences not found for user: " + userId));
    }

    public NotificationPreference updatePreferences(String userId, NotificationPreference preferences) {
        logger.debug("Updating notification preferences for user {}", userId);
        NotificationPreference existing = preferenceRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification preferences not found for user: " + userId));
        // Validate reminderMinutesBefore
        if (preferences.getReminderMinutesBefore() < 0) {
            throw new BadRequestException("Reminder minutes must be zero or positive");
        }
        // Update fields
        existing.setRideStatusEnabled(preferences.isRideStatusEnabled());
        existing.setBookingConfirmationEnabled(preferences.isBookingConfirmationEnabled());
        existing.setDepartureReminderEnabled(preferences.isDepartureReminderEnabled());
        existing.setEmailNotificationsEnabled(preferences.isEmailNotificationsEnabled());
        existing.setPushNotificationsEnabled(preferences.isPushNotificationsEnabled());
        existing.setReminderMinutesBefore(preferences.getReminderMinutesBefore());
        return preferenceRepository.save(existing);
    }
}
