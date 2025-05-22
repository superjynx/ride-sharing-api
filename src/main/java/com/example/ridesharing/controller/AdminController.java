package com.example.ridesharing.controller;

import com.example.ridesharing.model.User;
import com.example.ridesharing.model.Ride;
import com.example.ridesharing.model.Booking;
import com.example.ridesharing.repository.UserRepository;
import com.example.ridesharing.repository.RideRepository;
import com.example.ridesharing.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RideRepository rideRepository;
    @Autowired
    private BookingRepository bookingRepository;

    // Get all users (with optional pagination)
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // Deactivate user
    @PutMapping("/users/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable String id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        user.setActive(false);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User deactivated"));
    }

    // Get all rides (with optional pagination)
    @GetMapping("/rides")
    public ResponseEntity<List<Ride>> getAllRides() {
        return ResponseEntity.ok(rideRepository.findAll());
    }

    // Get all bookings (optional)
    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingRepository.findAll());
    }

    // Dashboard summary (optional)
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        long totalUsers = userRepository.count();
        long totalRides = rideRepository.count();
        long completedRides = rideRepository.findAll().stream().filter(r -> r.getStatus().toString().equals("COMPLETED")).count();
        long activeUsers = userRepository.findAll().stream().filter(User::isActive).count();
        return ResponseEntity.ok(Map.of(
            "totalUsers", totalUsers,
            "totalRides", totalRides,
            "completedRides", completedRides,
            "activeUsers", activeUsers
        ));
    }
}
