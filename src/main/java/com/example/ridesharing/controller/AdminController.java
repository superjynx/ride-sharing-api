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
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "username") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(userRepository.findAll(pageable));
    }

    // Deactivate user
    @PutMapping("/users/{username}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) return ResponseEntity.notFound().build();
        user.setActive(false);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User deactivated"));
    }

    // Activate user
    @PutMapping("/users/{username}/activate")
    public ResponseEntity<?> activateUser(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) return ResponseEntity.notFound().build();
        user.setActive(true);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User activated"));
    }

    // Get all rides (with optional pagination)
    @GetMapping("/rides")
    public ResponseEntity<Page<Ride>> getAllRides(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "departureTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(rideRepository.findAll(pageable));
    }

    // Get all bookings (optional)
    @GetMapping("/bookings")
    public ResponseEntity<Page<Booking>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(bookingRepository.findAll(pageable));
    }

    // Dashboard summary (optional)
    @GetMapping("/stats/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalRides = rideRepository.count();
        long totalBookings = bookingRepository.count();
        long activeUsers = userRepository.findAll().stream().filter(User::isActive).count();
        return ResponseEntity.ok(Map.of(
            "totalUsers", totalUsers,
            "totalRides", totalRides,
            "totalBookings", totalBookings,
            "activeUsers", activeUsers
        ));
    }

    // Get user stats
    @GetMapping("/stats/users/{username}")
    public ResponseEntity<Map<String, Object>> getUserStats(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) return ResponseEntity.notFound().build();

        long totalRides = rideRepository.countByDriverUsername(username);
        long totalBookings = bookingRepository.countByStudentId(username);

        return ResponseEntity.ok(Map.of(
            "totalRides", totalRides,
            "totalBookings", totalBookings
        ));
    }

    // Delete user
    @DeleteMapping("/users/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) return ResponseEntity.notFound().build();
        userRepository.delete(user);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }

    // Delete ride
    @DeleteMapping("/rides/{id}")
    public ResponseEntity<?> deleteRide(@PathVariable String id) {
        if (!rideRepository.existsById(id)) return ResponseEntity.notFound().build();
        rideRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Ride deleted"));
    }

    // Delete booking
    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable String id) {
        if (!bookingRepository.existsById(id)) return ResponseEntity.notFound().build();
        bookingRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Booking deleted"));
    }

    // Debug endpoint to check controller registration
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("AdminController is active");
    }
}
