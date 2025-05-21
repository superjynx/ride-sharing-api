package com.example.ridesharing.service;

import com.example.ridesharing.model.Notification;
import com.example.ridesharing.model.NotificationPreference;
import com.example.ridesharing.model.Ride;
import com.example.ridesharing.repository.NotificationRepository;
import com.example.ridesharing.repository.NotificationPreferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;

    public NotificationService(NotificationRepository notificationRepository, 
                             NotificationPreferenceRepository preferenceRepository) {
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
    }    // Send notification when ride status changes
    public void notifyRideStatusChange(Ride ride) {
        logger.debug("Sending ride status change notifications for ride {}", ride.getId());
        
        // Notify all passengers
        for (String passengerId : ride.getPassengers()) {
            NotificationPreference prefs = preferenceRepository.findByUserId(passengerId)
                .orElse(new NotificationPreference(passengerId));
                
            if (prefs.isRideStatusEnabled()) {
                createNotification(
                    passengerId,
                    "Ride Status Updated",
                    "Your ride from " + ride.getOrigin() + " to " + ride.getDestination() + 
                    " has been updated to " + ride.getStatus(),
                    Notification.NotificationType.RIDE_STATUS_CHANGE,
                    ride.getId()
                );
            }
        }
    }    // Send notification when ride is booked
    public void notifyRideBooked(Ride ride, String passengerId) {
        logger.debug("Sending ride booked notifications for ride {}", ride.getId());
        
        // Get preferences for both driver and passenger
        NotificationPreference driverPrefs = preferenceRepository.findByUserId(ride.getDriverUsername())
            .orElse(new NotificationPreference(ride.getDriverUsername()));
            
        NotificationPreference passengerPrefs = preferenceRepository.findByUserId(passengerId)
            .orElse(new NotificationPreference(passengerId));
        
        // Notify driver if they have booking notifications enabled
        if (driverPrefs.isBookingConfirmationEnabled()) {
            createNotification(
                ride.getDriverUsername(),
                "New Booking",
                "A new passenger has booked your ride to " + ride.getDestination(),
                Notification.NotificationType.RIDE_BOOKED,
                ride.getId()
            );
        }

        // Notify passenger if they have booking notifications enabled
        if (passengerPrefs.isBookingConfirmationEnabled()) {
            createNotification(
                passengerId,
                "Booking Confirmed",
                "Your booking for the ride to " + ride.getDestination() + " has been confirmed",
                Notification.NotificationType.BOOKING_CONFIRMED,
                ride.getId()
            );
        }
    }    // Send departure reminder
    public void sendDepartureReminder(Ride ride) {
        logger.debug("Sending departure reminders for ride {}", ride.getId());
        
        // Get driver preferences
        NotificationPreference driverPrefs = preferenceRepository.findByUserId(ride.getDriverUsername())
            .orElse(new NotificationPreference(ride.getDriverUsername()));

        if (driverPrefs.isDepartureReminderEnabled()) {
            String driverMessage = String.format(
                "Reminder: Your ride from %s to %s departs in %d minutes",
                ride.getOrigin(),
                ride.getDestination(),
                driverPrefs.getReminderMinutesBefore()
            );

            // Notify driver
            createNotification(
                ride.getDriverUsername(),
                "Departure Reminder",
                driverMessage,
                Notification.NotificationType.RIDE_REMINDER,
                ride.getId()
            );
        }

        // Notify all passengers based on their preferences
        for (String passengerId : ride.getPassengers()) {
            NotificationPreference passengerPrefs = preferenceRepository.findByUserId(passengerId)
                .orElse(new NotificationPreference(passengerId));
                
            if (passengerPrefs.isDepartureReminderEnabled()) {
                String passengerMessage = String.format(
                    "Reminder: Your ride from %s to %s departs in %d minutes",
                    ride.getOrigin(),
                    ride.getDestination(),
                    passengerPrefs.getReminderMinutesBefore()
                );
                
                createNotification(
                    passengerId,
                    "Departure Reminder",
                    passengerMessage,
                    Notification.NotificationType.RIDE_REMINDER,
                    ride.getId()
                );
            }
        }
    }

    // Get user's notifications
    public Page<Notification> getUserNotifications(String userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
    }

    // Get unread notification count
    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    // Mark notification as read
    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    // Mark all notifications as read
    public void markAllAsRead(String userId) {
        notificationRepository.findByUserIdAndReadFalse(userId).forEach(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    // Helper method to create and save a notification
    private Notification createNotification(
            String userId, 
            String title, 
            String message, 
            Notification.NotificationType type,
            String rideId) {
        Notification notification = new Notification(userId, title, message, type, rideId);
        return notificationRepository.save(notification);
    }
}
