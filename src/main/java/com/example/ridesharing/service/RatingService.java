package com.example.ridesharing.service;

import com.example.ridesharing.exception.ResourceNotFoundException;
import com.example.ridesharing.exception.BadRequestException;
import com.example.ridesharing.exception.ConflictException;
import com.example.ridesharing.exception.UnauthorizedException;
import com.example.ridesharing.model.Rating;
import com.example.ridesharing.model.Ride;
import com.example.ridesharing.model.User;
import com.example.ridesharing.repository.RatingRepository;
import com.example.ridesharing.repository.RideRepository;
import com.example.ridesharing.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RatingService {
    private static final Logger logger = LoggerFactory.getLogger(RatingService.class);

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final RideRepository rideRepository;

    public RatingService(RatingRepository ratingRepository, UserRepository userRepository, RideRepository rideRepository) {
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.rideRepository = rideRepository;
    }

    /**
     * Submit a rating from one user to another for a specific ride
     */
    public Rating submitRating(String rideId, String fromUserId, String toUserId, int score, String comment) {
        logger.debug("Submitting rating for ride {}: {} -> {}, score: {}", rideId, fromUserId, toUserId, score);
        
        // Verify the ride exists
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found"));
        
        // Verify both users exist
        User fromUser = userRepository.findByUsername(fromUserId);
        if (fromUser == null) {
            throw new ResourceNotFoundException("Rating user not found");
        }
        
        User toUser = userRepository.findByUsername(toUserId);
        if (toUser == null) {
            throw new ResourceNotFoundException("Rated user not found");
        }
        
        // Verify the ride is completed
        if (ride.getStatus() != com.example.ridesharing.enums.RideStatus.COMPLETED) {
            throw new BadRequestException("Can only rate completed rides");
        }
        
        // Verify the users were part of the ride
        boolean isDriverRating = fromUserId.equals(ride.getDriverUsername());
        boolean isPassengerRating = ride.getPassengers().contains(fromUserId);
        
        if (!isDriverRating && !isPassengerRating) {
            throw new UnauthorizedException("Only ride participants can submit ratings");
        }
        
        // If from driver, verify to user was a passenger
        if (isDriverRating && !ride.getPassengers().contains(toUserId)) {
            throw new BadRequestException("Can only rate passengers of this ride");
        }
        
        // If from passenger, verify to user was the driver
        if (isPassengerRating && !toUserId.equals(ride.getDriverUsername())) {
            throw new BadRequestException("Can only rate the driver of this ride");
        }
        
        // Check if rating already exists (prevent multiple ratings)
        if (ratingRepository.existsByRideIdAndFromUserIdAndToUserId(rideId, fromUserId, toUserId)) {
            throw new ConflictException("You have already rated this user for this ride");
        }
        
        // Create and save the rating
        Rating rating = new Rating(rideId, fromUserId, toUserId, score, comment);
        Rating savedRating = ratingRepository.save(rating);
        
        // Update the user's average rating
        updateUserRating(toUser);
        
        logger.info("Rating submitted successfully. ID: {}", savedRating.getId());
        return savedRating;
    }
    
    /**
     * Get a specific rating
     */
    public Rating getRatingById(String ratingId) {
        return ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found"));
    }
    
    /**
     * Get all ratings for a ride
     */
    public List<Rating> getRatingsForRide(String rideId) {
        return ratingRepository.findByRideId(rideId);
    }
    
    /**
     * Get all ratings received by a user
     */
    public Page<Rating> getRatingsForUser(String userId, Pageable pageable) {
        return ratingRepository.findByToUserId(userId, pageable);
    }
    
    /**
     * Get all ratings given by a user
     */
    public Page<Rating> getRatingsFromUser(String userId, Pageable pageable) {
        return ratingRepository.findByFromUserId(userId, pageable);
    }
    
    /**
     * Check if a user has already rated another user for a ride
     */
    public boolean hasUserRated(String rideId, String fromUserId, String toUserId) {
        return ratingRepository.existsByRideIdAndFromUserIdAndToUserId(rideId, fromUserId, toUserId);
    }
    
    /**
     * Update a user's average rating based on all received ratings
     */
    private void updateUserRating(User user) {
        List<Rating> userRatings = ratingRepository.findByToUserId(user.getUsername());
        
        if (userRatings.isEmpty()) {
            user.setAverageRating(0.0);
            user.setTotalRatings(0);
        } else {
            double sum = userRatings.stream().mapToInt(Rating::getScore).sum();
            double average = sum / userRatings.size();
            
            user.setAverageRating(average);
            user.setTotalRatings(userRatings.size());
        }
        
        userRepository.save(user);
        logger.debug("Updated rating for user {}: average={}, total={}", 
                user.getUsername(), user.getAverageRating(), user.getTotalRatings());
    }
}