package com.example.ridesharing.dto;

/**
 * Data Transfer Object for user profile information
 * Contains only the information that should be publicly accessible
 */
public class UserProfileDTO {
    private String username;
    private String fullName;
    private String profilePictureUrl;
    private String bio;
    private double averageRating;
    private int totalRatings;
    private boolean isDriver;
    
    // Driver-specific fields (only populated if isDriver is true)
    private String vehicleType;
    private String vehicleModel;
    
    public UserProfileDTO() {
    }
    
    // Getters and setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }
    
    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public double getAverageRating() {
        return averageRating;
    }
    
    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }
    
    public int getTotalRatings() {
        return totalRatings;
    }
    
    public void setTotalRatings(int totalRatings) {
        this.totalRatings = totalRatings;
    }
    
    public boolean getIsDriver() {
        return isDriver;
    }
    
    public void setIsDriver(boolean isDriver) {
        this.isDriver = isDriver;
    }
    
    public String getVehicleType() {
        return vehicleType;
    }
    
    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }
    
    public String getVehicleModel() {
        return vehicleModel;
    }
    
    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }
} 