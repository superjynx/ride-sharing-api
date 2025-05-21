package com.example.ridesharing.scheduler;

import com.example.ridesharing.enums.RideStatus;
import com.example.ridesharing.model.Ride;
import com.example.ridesharing.repository.RideRepository;
import com.example.ridesharing.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RideScheduler {
    private static final Logger logger = LoggerFactory.getLogger(RideScheduler.class);
    
    private final RideRepository rideRepository;
    private final NotificationService notificationService;
    private final MongoTemplate mongoTemplate;

    public RideScheduler(RideRepository rideRepository, NotificationService notificationService, MongoTemplate mongoTemplate) {
        this.rideRepository = rideRepository;
        this.notificationService = notificationService;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Scheduled task that runs every 5 minutes to check for upcoming rides
     * and send departure reminders 30 minutes before departure
     */
    @Scheduled(fixedRate = 300000) // 5 minutes in milliseconds
    public void sendDepartureReminders() {
        logger.debug("Checking for upcoming rides to send reminders");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyMinutesFromNow = now.plusMinutes(30);
        
        // Find scheduled rides departing in 30 minutes
        Query query = new Query();
        query.addCriteria(Criteria.where("status").is(RideStatus.SCHEDULED));
        query.addCriteria(Criteria.where("departureTime").gte(now).lt(thirtyMinutesFromNow));
        
        List<Ride> upcomingRides = mongoTemplate.find(query, Ride.class);
        
        for (Ride ride : upcomingRides) {
            logger.debug("Sending departure reminder for ride {}", ride.getId());
            notificationService.sendDepartureReminder(ride);
        }
    }
    
    /**
     * Scheduled task that runs daily to clean up expired rides
     * This marks rides as COMPLETED if they have departed and their status is still IN_PROGRESS
     * It also removes rides that have departed more than 30 days ago (data retention policy)
     */
    @Scheduled(cron = "0 0 1 * * ?") // Run at 1 AM every day
    public void cleanupExpiredRides() {
        logger.debug("Starting expired rides cleanup job");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneDayAgo = now.minusDays(1);
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        
        // 1. Mark rides as COMPLETED if they're IN_PROGRESS and departure time + expected duration is in the past
        Query inProgressQuery = new Query();
        inProgressQuery.addCriteria(Criteria.where("status").is(RideStatus.IN_PROGRESS));
        inProgressQuery.addCriteria(Criteria.where("departureTime").lt(oneDayAgo));
        
        List<Ride> expiredInProgressRides = mongoTemplate.find(inProgressQuery, Ride.class);
        logger.info("Found {} expired IN_PROGRESS rides to mark as COMPLETED", expiredInProgressRides.size());
        
        for (Ride ride : expiredInProgressRides) {
            ride.setStatus(RideStatus.COMPLETED);
            rideRepository.save(ride);
            logger.debug("Automatically marked ride {} as COMPLETED", ride.getId());
        }
        
        // 2. Optional: Remove very old rides for data cleanup (30+ days old)
        // Comment out this section if you want to keep all historical data
        Query oldRidesQuery = new Query();
        oldRidesQuery.addCriteria(Criteria.where("departureTime").lt(thirtyDaysAgo));
        oldRidesQuery.addCriteria(Criteria.where("status").in(RideStatus.COMPLETED, RideStatus.CANCELLED));
        
        long deletedCount = mongoTemplate.remove(oldRidesQuery, Ride.class).getDeletedCount();
        if (deletedCount > 0) {
            logger.info("Removed {} old completed/cancelled rides (>30 days old)", deletedCount);
        }
    }
}
