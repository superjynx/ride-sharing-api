package com.example.ridesharing.repository;

import com.example.ridesharing.model.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends MongoRepository<Rating, String> {
    // Find a rating by ride and users
    Optional<Rating> findByRideIdAndFromUserIdAndToUserId(String rideId, String fromUserId, String toUserId);
    
    // Find all ratings for a specific ride
    List<Rating> findByRideId(String rideId);
    
    // Find all ratings received by a user
    List<Rating> findByToUserId(String userId);
    
    // Find all ratings received by a user (paginated)
    Page<Rating> findByToUserId(String userId, Pageable pageable);
    
    // Find all ratings given by a user
    List<Rating> findByFromUserId(String userId);
    
    // Find all ratings given by a user (paginated)
    Page<Rating> findByFromUserId(String userId, Pageable pageable);
    
    // Check if a user has already rated another user for a ride
    boolean existsByRideIdAndFromUserIdAndToUserId(String rideId, String fromUserId, String toUserId);
} 