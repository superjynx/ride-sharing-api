package com.example.ridesharing.controller;

import com.example.ridesharing.dto.PagedResponse;
import com.example.ridesharing.enums.RideStatus;
import com.example.ridesharing.model.Ride;
import com.example.ridesharing.service.RideService;
import com.example.ridesharing.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/rides")
public class RideController {
    private static final Logger logger = LoggerFactory.getLogger(RideController.class);
    private final RideService rideService;
    private final UserRepository userRepository;

    public RideController(RideService rideService, UserRepository userRepository) {
        this.rideService = rideService;
        this.userRepository = userRepository;
    }

    // Create a new ride
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_DRIVER')")
    public ResponseEntity<Ride> createRide(@Valid @RequestBody Ride ride) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        ride.setDriverUsername(username);
        ride.setStatus(RideStatus.SCHEDULED);
        ride.setMaxPassengers(ride.getAvailableSeats()); // Set maxPassengers to match availableSeats
        if (ride.getPrice() < 0) {
            ride.setPrice(0.0); // Set a default price if negative
        }
        return ResponseEntity.ok(rideService.createRide(ride));
    }

    // Get a ride by ID
    @GetMapping("/{rideId}")
    @PreAuthorize("hasAnyAuthority('ROLE_DRIVER', 'ROLE_STUDENT')")
    public ResponseEntity<Ride> getRideById(@PathVariable String rideId) {
        logger.debug("Fetching ride with ID: {}", rideId);
        return rideService.getRideById(rideId)
                .map(ride -> ResponseEntity.ok(ride))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{rideId}/passengers")
    @PreAuthorize("hasAuthority('ROLE_DRIVER')")
    public ResponseEntity<Set<String>> getPassengers(@PathVariable String rideId, Authentication authentication) {
        Set<String> passengers = rideService.getPassengersByRideId(rideId, authentication);
        return ResponseEntity.ok(passengers);
    }
    
    // Update ride status
    @PatchMapping("/{rideId}/status")
    @PreAuthorize("hasAuthority('ROLE_DRIVER')")
    public ResponseEntity<Ride> updateRideStatus(
            @PathVariable String rideId,
            @RequestParam RideStatus status,
            Authentication authentication) {
        logger.debug("Updating ride {} status to {}", rideId, status);
        String driverUsername = authentication.getName();
        Ride updatedRide = rideService.updateRideStatus(rideId, driverUsername, status);
        return ResponseEntity.ok(updatedRide);
    }

    // Mark ride as completed
    @PostMapping("/{rideId}/complete")
    @PreAuthorize("hasAuthority('ROLE_DRIVER')")
    public ResponseEntity<Ride> completeRide(@PathVariable String rideId, Authentication authentication) {
        logger.debug("Marking ride {} as completed", rideId);
        String driverUsername = authentication.getName();
        Ride completedRide = rideService.updateRideStatus(rideId, driverUsername, RideStatus.COMPLETED);
        return ResponseEntity.ok(completedRide);
    }

    // Book a ride
    @PostMapping("/{rideId}/book")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<?> bookRide(@PathVariable String rideId, Authentication authentication) {
        logger.debug("Booking ride {} for student {}", rideId, authentication.getName());
        try {
            Ride updatedRide = rideService.bookRide(rideId, authentication.getName());
            // Fetch user details
            com.example.ridesharing.model.User user = null;
            try {
                user = userRepository.findByUsername(authentication.getName());
            } catch (Exception ex) {
                logger.warn("User not found for username: {}", authentication.getName());
            }
            Map<String, Object> userDetails = user != null ? Map.of(
                "username", user.getUsername() != null ? user.getUsername() : "",
                "fullName", user.getFullName() != null ? user.getFullName() : "",
                "email", user.getEmail() != null ? user.getEmail() : "",
                "role", user.getRole() != null ? user.getRole() : ""
            ) : Map.of("username", authentication.getName());
            return ResponseEntity.ok(Map.of(
                "message", "Ride booked successfully",
                "ride", updatedRide,
                "user", userDetails,
                "availableSeats", updatedRide.getAvailableSeats(),
                "status", updatedRide.getStatus(),
                "passengers", updatedRide.getPassengers()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Driver cancels their own ride
    @PostMapping("/{rideId}/cancel")
    @PreAuthorize("hasAuthority('ROLE_DRIVER')")
    public ResponseEntity<?> cancelRide(@PathVariable String rideId, Authentication authentication) {
        String driverUsername = authentication.getName();
        Ride ride = rideService.getRideById(rideId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found"));
        if (!ride.getDriverUsername().equals(driverUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the driver of this ride");
        }
        rideService.updateRideStatus(rideId, driverUsername, RideStatus.CANCELLED);
        return ResponseEntity.ok(Map.of("message", "Ride cancelled", "rideId", rideId));
    }

    // Passenger unbooks (removes themselves) from a ride
    @PostMapping("/{rideId}/unbook")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<?> unbookRide(@PathVariable String rideId, Authentication authentication) {
        String username = authentication.getName();
        try {
            rideService.cancelBooking(rideId, username);
            return ResponseEntity.ok(Map.of("message", "Unbooked from ride", "rideId", rideId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Driver removes a specific passenger from their ride
    @PostMapping("/{rideId}/remove-passenger")
    @PreAuthorize("hasAuthority('ROLE_DRIVER')")
    public ResponseEntity<?> removePassengerFromRide(
            @PathVariable String rideId,
            @RequestParam String passengerUsername,
            Authentication authentication) {
        String driverUsername = authentication.getName();
        try {
            Ride updatedRide = rideService.removePassengerByDriver(rideId, driverUsername, passengerUsername);
            return ResponseEntity.ok(Map.of(
                "message", "Passenger removed from ride",
                "rideId", rideId,
                "removedPassenger", passengerUsername,
                "availableSeats", updatedRide.getAvailableSeats(),
                "status", updatedRide.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Search rides with date filtering
    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_DRIVER')")
    public ResponseEntity<PagedResponse<Ride>> searchRides(
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime fromDepartureTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime toDepartureTime,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minSeats,
            @RequestParam(required = false, defaultValue = "false") Boolean includeFullRides,
            @RequestParam(required = false) List<RideStatus> statuses,
            @RequestParam(required = false, defaultValue = "false") boolean includePastRides,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "departureTime") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir
    ) {
        logger.debug("Search request with date filtering - from: {}, to: {}, includePastRides: {}", 
                    fromDepartureTime, toDepartureTime, includePastRides);

        // Validate pagination parameters
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page number cannot be negative");
        }
        if (size < 1 || size > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page size must be between 1 and 50");
        }

        // Get paginated and filtered results
        Page<Ride> ridePage = rideService.searchRides(
                origin,
                destination,
                fromDepartureTime,
                toDepartureTime,
                maxPrice,
                minSeats,
                includeFullRides,
                statuses,
                includePastRides,
                page,
                size,
                sortBy,
                sortDir
        );

        // Convert to PagedResponse
        PagedResponse<Ride> response = new PagedResponse<>(
                ridePage.getContent(),
                ridePage.getNumber(),
                ridePage.getSize(),
                ridePage.getTotalElements(),
                ridePage.getTotalPages(),
                ridePage.isLast()
        );

        return ResponseEntity.ok(response);
    }

    // Get user's rides (as driver or passenger)
    @GetMapping("/my-rides")
    @PreAuthorize("hasAnyAuthority('ROLE_DRIVER', 'ROLE_STUDENT')")
    public ResponseEntity<PagedResponse<Ride>> getMyRides(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "departureTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        logger.debug("Fetching rides for user {}", authentication.getName());
        
        // Validate pagination parameters
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page number cannot be negative");
        }
        if (size < 1 || size > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page size must be between 1 and 50");
        }

        // Determine if user is a driver
        boolean isDriver = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DRIVER"));

        // Create pageable with sorting
        Pageable pageable = PageRequest.of(page, size, 
            Sort.by(Sort.Direction.fromString(sortDir), sortBy));

        // Get paginated rides
        Page<Ride> ridePage = rideService.getUserRides(
            authentication.getName(),
            isDriver,
            pageable
        );

        // Convert to PagedResponse
        PagedResponse<Ride> response = new PagedResponse<>(
            ridePage.getContent(),
            ridePage.getNumber(),
            ridePage.getSize(),
            ridePage.getTotalElements(),
            ridePage.getTotalPages(),
            ridePage.isLast()
        );

        return ResponseEntity.ok(response);
    }

    // Get passenger booking history (all rides booked by the user)
    @GetMapping("/my-bookings")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<PagedResponse<Ride>> getMyBookings(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "departureTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) List<RideStatus> statuses) {
        logger.debug("Fetching booking history for passenger {}", authentication.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        Page<Ride> ridePage = rideService.getUserRidesWithStatus(authentication.getName(), false, statuses, pageable);
        PagedResponse<Ride> response = new PagedResponse<>(
            ridePage.getContent(),
            ridePage.getNumber(),
            ridePage.getSize(),
            ridePage.getTotalElements(),
            ridePage.getTotalPages(),
            ridePage.isLast()
        );
        return ResponseEntity.ok(response);
    }

    // Get driver ride history (completed rides hosted by the driver)
    @GetMapping("/my-driven-rides")
    @PreAuthorize("hasAuthority('ROLE_DRIVER')")
    public ResponseEntity<PagedResponse<Ride>> getMyDrivenRides(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "departureTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) List<RideStatus> statuses) {
        logger.debug("Fetching completed rides for driver {}", authentication.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        // Default to COMPLETED if no status filter is provided
        List<RideStatus> statusFilter = (statuses == null || statuses.isEmpty()) ? List.of(RideStatus.COMPLETED) : statuses;
        Page<Ride> ridePage = rideService.getUserRidesWithStatus(authentication.getName(), true, statusFilter, pageable);
        PagedResponse<Ride> response = new PagedResponse<>(
            ridePage.getContent(),
            ridePage.getNumber(),
            ridePage.getSize(),
            ridePage.getTotalElements(),
            ridePage.getTotalPages(),
            ridePage.isLast()
        );
        return ResponseEntity.ok(response);
    }

    // List all possible ride statuses
    @GetMapping("/statuses")
    public ResponseEntity<?> getRideStatuses() {
        return ResponseEntity.ok(java.util.Arrays.stream(com.example.ridesharing.enums.RideStatus.values())
            .map(status -> Map.of(
                "name", status.name(),
                "description", getStatusDescription(status)
            )).toList());
    }

    private String getStatusDescription(com.example.ridesharing.enums.RideStatus status) {
        switch (status) {
            case SCHEDULED: return "Initial state when ride is created";
            case IN_PROGRESS: return "Ride has started";
            case COMPLETED: return "Ride has finished";
            case CANCELLED: return "Ride was cancelled";
            default: return "Unknown status";
        }
    }
}
