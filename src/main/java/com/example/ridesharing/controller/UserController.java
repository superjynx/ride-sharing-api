package com.example.ridesharing.controller;

import com.example.ridesharing.dto.PagedResponse;
import com.example.ridesharing.dto.UserProfileDTO;
import com.example.ridesharing.model.Rating;
import com.example.ridesharing.model.User;
import com.example.ridesharing.repository.UserRepository;
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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepository;
    private final RatingService ratingService;

    public UserController(UserRepository userRepository, RatingService ratingService) {
        this.userRepository = userRepository;
        this.ratingService = ratingService;
    }

    /**
     * Get current user profile
     */
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        logger.debug("Fetching profile for current user: {}", username);
        
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        
        // Don't return password in response
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    /**
     * Update current user profile
     */
    @PutMapping("/me")
    public ResponseEntity<User> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody User updatedUser) {
        String username = authentication.getName();
        logger.debug("Updating profile for current user: {}", username);
        
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        
        // Update allowed fields
        user.setFullName(updatedUser.getFullName());
        user.setEmail(updatedUser.getEmail());
        user.setPhoneNumber(updatedUser.getPhoneNumber());
        user.setProfilePictureUrl(updatedUser.getProfilePictureUrl());
        user.setBio(updatedUser.getBio());
        
        // Update driver-specific fields if user is a driver
        if (user.getRole().toString().equals("ROLE_DRIVER")) {
            user.setLicenseNumber(updatedUser.getLicenseNumber());
            user.setVehicleType(updatedUser.getVehicleType());
            user.setVehicleModel(updatedUser.getVehicleModel());
            user.setVehiclePlate(updatedUser.getVehiclePlate());
        }
        
        User savedUser = userRepository.save(user);
        savedUser.setPassword(null); // Don't return password
        return ResponseEntity.ok(savedUser);
    }

    /**
     * Get a user's public profile by username
     */
    @GetMapping("/{username}")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable String username) {
        logger.debug("Fetching profile for user: {}", username);
        
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        
        // Create DTO with only the public information
        UserProfileDTO profile = new UserProfileDTO();
        profile.setUsername(user.getUsername());
        profile.setFullName(user.getFullName());
        profile.setProfilePictureUrl(user.getProfilePictureUrl());
        profile.setBio(user.getBio());
        profile.setAverageRating(user.getAverageRating());
        profile.setTotalRatings(user.getTotalRatings());
        
        // Add driver-specific info if the user is a driver
        if (user.getRole().toString().equals("ROLE_DRIVER")) {
            profile.setIsDriver(true);
            profile.setVehicleType(user.getVehicleType());
            profile.setVehicleModel(user.getVehicleModel());
        } else {
            profile.setIsDriver(false);
        }
        
        return ResponseEntity.ok(profile);
    }

    /**
     * Get ratings received by a user
     */
    @GetMapping("/{username}/ratings")
    public ResponseEntity<PagedResponse<Rating>> getUserRatings(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        logger.debug("Fetching ratings for user: {}", username);
        
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        
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

    // Legacy endpoints for backward compatibility
    @PostMapping("/addUser")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addUser(@Valid @RequestBody User user) {
        logger.debug("Creating new user: {}", user.getUsername());
        userRepository.save(user);
        return ResponseEntity.ok("User saved successfully");
    }

    @GetMapping("/getUser/{id}")
    public ResponseEntity<User> getUser(@PathVariable String id) {
        logger.debug("Fetching user by ID: {}", id);
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setPassword(null); // Don't return password
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
