package com.example.ridesharing.controller;

import com.example.ridesharing.dto.LoginRequest;
import com.example.ridesharing.dto.SignupRequest;
import com.example.ridesharing.model.User;
import com.example.ridesharing.model.Role;
import com.example.ridesharing.repository.UserRepository;
import com.example.ridesharing.util.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    // Signup endpoint
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) {
        logger.debug("Processing signup request for username: {}", signupRequest.getUsername());
        
        // Check if user already exists
        User existingUser = userRepository.findByUsername(signupRequest.getUsername());
        if (existingUser != null) {
            logger.warn("User already exists: {}", signupRequest.getUsername());
            Map<String, String> response = new HashMap<>();
            response.put("error", "User already exists");
            return ResponseEntity.badRequest().body(response);
        }

        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        if (signupRequest.getRole() != null) {
            user.setRole(Role.valueOf(signupRequest.getRole()));
        } else {
            user.setRole(Role.ROLE_STUDENT);
        }
        
        logger.debug("Creating user with username: {}, role: {}", user.getUsername(), user.getRole());
        userRepository.save(user);
        
        // Verify user was saved
        User savedUser = userRepository.findByUsername(user.getUsername());
        if (savedUser == null) {
            logger.error("Failed to save user: {}", user.getUsername());
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to create user");
            return ResponseEntity.badRequest().body(response);
        }
        
        logger.debug("User created successfully: {}, role: {}", savedUser.getUsername(), savedUser.getRole());
        Map<String, String> response = new HashMap<>();
        response.put("message", "User created successfully");
        response.put("role", savedUser.getRole().toString());
        return ResponseEntity.ok(response);
    }

    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        logger.debug("Processing login request for username: {}", loginRequest.getUsername());
        
        // Check if user exists
        User user = userRepository.findByUsername(loginRequest.getUsername());
        if (user == null) {
            logger.error("User not found: {}", loginRequest.getUsername());
            Map<String, String> response = new HashMap<>();
            response.put("error", "User not found");
            return ResponseEntity.badRequest().body(response);
        }
        
        logger.debug("Found user: {}, role: {}", user.getUsername(), user.getRole());
        
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            logger.debug("Authentication successful for user: {}", loginRequest.getUsername());
        } catch (Exception e) {
            logger.error("Authentication failed for user: {}", loginRequest.getUsername(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Invalid username or password");
            return ResponseEntity.badRequest().body(response);
        }
        
        String token = jwtUtils.generateToken(user.getUsername(), user.getRole());
        logger.debug("Generated token for user: {}, role: {}", user.getUsername(), user.getRole());
        
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("username", user.getUsername());
        response.put("role", user.getRole().toString());
        logger.debug("Login successful for user: {} with role: {}", user.getUsername(), user.getRole());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/debug/createTestUsers")
    public ResponseEntity<?> createTestUsers() {
        // Create a test student if it doesn't exist
        if (userRepository.findByUsername("student2") == null) {
            User student = new User();
            student.setUsername("student2");
            student.setPassword(passwordEncoder.encode("password123"));
            student.setRole(Role.ROLE_STUDENT);
            userRepository.save(student);
        }
        
        // Create a test driver if it doesn't exist
        if (userRepository.findByUsername("testdriver1") == null) {
            User driver = new User();
            driver.setUsername("testdriver1");
            driver.setPassword(passwordEncoder.encode("password123"));
            driver.setRole(Role.ROLE_DRIVER);
            userRepository.save(driver);
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Test users created or verified successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/debug/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestParam String username, @RequestParam String newPassword) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Password reset for user " + username));
    }
}
