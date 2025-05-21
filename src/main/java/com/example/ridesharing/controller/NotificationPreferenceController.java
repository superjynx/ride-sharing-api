package com.example.ridesharing.controller;

import com.example.ridesharing.model.NotificationPreference;
import com.example.ridesharing.service.NotificationPreferenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notification-preferences")
public class NotificationPreferenceController {
    private static final Logger logger = LoggerFactory.getLogger(NotificationPreferenceController.class);
    private final NotificationPreferenceService preferenceService;

    public NotificationPreferenceController(NotificationPreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @GetMapping
    public ResponseEntity<NotificationPreference> getPreferences(Authentication authentication) {
        String userId = authentication.getName();
        logger.debug("Fetching notification preferences for user {}", userId);
        NotificationPreference preferences = preferenceService.getPreferences(userId);
        return ResponseEntity.ok(preferences);
    }

    @PutMapping
    public ResponseEntity<NotificationPreference> updatePreferences(
            Authentication authentication,
            @Valid @RequestBody NotificationPreference preferences) {
        String userId = authentication.getName();
        logger.debug("Updating notification preferences for user {}", userId);
        NotificationPreference updated = preferenceService.updatePreferences(userId, preferences);
        return ResponseEntity.ok(updated);
    }
}
