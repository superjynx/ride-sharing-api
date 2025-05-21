package com.example.ridesharing.controller;

import com.example.ridesharing.dto.PagedResponse;
import com.example.ridesharing.model.Notification;
import com.example.ridesharing.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<Notification>> getNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String userId = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationService.getUserNotifications(userId, pageable);
        
        PagedResponse<Notification> response = new PagedResponse<>(
            notifications.getContent(),
            notifications.getNumber(),
            notifications.getSize(),
            notifications.getTotalElements(),
            notifications.getTotalPages(),
            notifications.isLast()
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        String userId = authentication.getName();
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            Authentication authentication,
            @PathVariable String notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        String userId = authentication.getName();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
}
