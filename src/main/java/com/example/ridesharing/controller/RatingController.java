package com.example.ridesharing.controller;

import com.example.ridesharing.dto.PagedResponse;
import com.example.ridesharing.model.Rating;
import com.example.ridesharing.service.RatingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {
    private static final Logger logger = LoggerFactory.getLogger(RatingController.class);
    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    /**
     * Submit a rating for a ride
     */
    @PostMapping("/rides/{rideId}")
    @PreAuthorize("hasAnyAuthority('ROLE_DRIVER', 'ROLE_STUDENT')")
    public ResponseEntity<Rating> submitRating(
            @PathVariable String rideId,
            @RequestParam String toUserId,
            @RequestBody Map<String, Object> ratingData,
            Authentication authentication) {
        
        String fromUserId = authentication.getName();
        logger.debug("Rating submission from {} to {} for ride {}", fromUserId, toUserId, rideId);
        
        // Extract rating data
        Integer score = (Integer) ratingData.get("score");
        String comment = (String) ratingData.get("comment");
        
        // Validate score
        if (score == null || score < 1 || score > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating score must be between 1 and 5");
        }
        
        Rating rating = ratingService.submitRating(rideId, fromUserId, toUserId, score, comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(rating);
    }
    
    /**
     * Check if a user has already rated another user for a specific ride
     */
    @GetMapping("/rides/{rideId}/check")
    @PreAuthorize("hasAnyAuthority('ROLE_DRIVER', 'ROLE_STUDENT')")
    public ResponseEntity<Map<String, Boolean>> checkRating(
            @PathVariable String rideId,
            @RequestParam String toUserId,
            Authentication authentication) {
        
        String fromUserId = authentication.getName();
        logger.debug("Checking if {} has rated {} for ride {}", fromUserId, toUserId, rideId);
        
        boolean hasRated = ratingService.hasUserRated(rideId, fromUserId, toUserId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasRated", hasRated);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all ratings for a specific ride
     */
    @GetMapping("/rides/{rideId}")
    @PreAuthorize("hasAnyAuthority('ROLE_DRIVER', 'ROLE_STUDENT')")
    public ResponseEntity<List<Rating>> getRideRatings(@PathVariable String rideId) {
        logger.debug("Fetching all ratings for ride {}", rideId);
        List<Rating> ratings = ratingService.getRatingsForRide(rideId);
        return ResponseEntity.ok(ratings);
    }
    
    /**
     * Get all ratings given by the current user
     */
    @GetMapping("/given")
    @PreAuthorize("hasAnyAuthority('ROLE_DRIVER', 'ROLE_STUDENT')")
    public ResponseEntity<PagedResponse<Rating>> getRatingsGiven(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        String username = authentication.getName();
        logger.debug("Fetching ratings given by user {}", username);
        
        Pageable pageable = PageRequest.of(page, size, 
                Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        
        Page<Rating> ratingsPage = ratingService.getRatingsFromUser(username, pageable);
        
        PagedResponse<Rating> response = new PagedResponse<>(
                ratingsPage.getContent(),
                ratingsPage.getNumber(),
                ratingsPage.getSize(),
                ratingsPage.getTotalElements(),
                ratingsPage.getTotalPages(),
                ratingsPage.isLast()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all ratings received by the current user
     */
    @GetMapping("/received")
    @PreAuthorize("hasAnyAuthority('ROLE_DRIVER', 'ROLE_STUDENT')")
    public ResponseEntity<PagedResponse<Rating>> getRatingsReceived(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        String username = authentication.getName();
        logger.debug("Fetching ratings received by user {}", username);
        
        Pageable pageable = PageRequest.of(page, size, 
                Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        
        Page<Rating> ratingsPage = ratingService.getRatingsForUser(username, pageable);
        
        PagedResponse<Rating> response = new PagedResponse<>(
                ratingsPage.getContent(),
                ratingsPage.getNumber(),
                ratingsPage.getSize(),
                ratingsPage.getTotalElements(),
                ratingsPage.getTotalPages(),
                ratingsPage.isLast()
        );
        
        return ResponseEntity.ok(response);
    }
} 