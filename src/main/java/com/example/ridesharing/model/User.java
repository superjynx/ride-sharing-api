package com.example.ridesharing.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @jakarta.validation.constraints.NotBlank(message = "Username is required")
    private String username;

    @jakarta.validation.constraints.NotBlank(message = "Password is required")
    private String password;

    private Role role = Role.ROLE_STUDENT;  // Default role if not provided
    private boolean active = true;

    // Profile fields
    @jakarta.validation.constraints.NotBlank(message = "Full name is required")
    private String fullName;

    @jakarta.validation.constraints.Email(message = "Invalid email format")
    private String email;

    @jakarta.validation.constraints.Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone number")
    private String phoneNumber;

    private String profilePictureUrl;
    private String bio;
    
    // Driver-specific fields (only used when role is ROLE_DRIVER)
    private String licenseNumber;
    private String vehicleType;
    private String vehicleModel;
    private String vehiclePlate;
    
    // Rating fields
    private double averageRating = 0.0;  // Average rating score (0-5)
    private int totalRatings = 0;        // Total number of ratings received

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
