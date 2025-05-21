package com.example.ridesharing.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;

import java.time.LocalDateTime;

/**
 * Model representing a rating given by one user to another after a ride
 */
@Document(collection = "ratings")
@CompoundIndexes({
    @CompoundIndex(name = "rideId_fromUser_toUser", def = "{'rideId': 1, 'fromUserId': 1, 'toUserId': 1}", unique = true)
})
public class Rating {
    @Id
    private String id;
    
    private String rideId;           // The ride this rating is associated with
    private String fromUserId;        // User ID of the rater (who gave the rating)
    private String toUserId;          // User ID of the ratee (who received the rating)
    
    private int score;                // Rating score (1-5)
    private String comment;           // Optional comment with the rating
    private LocalDateTime timestamp;  // When the rating was submitted
    
    public Rating() {
        this.timestamp = LocalDateTime.now();
    }
    
    public Rating(String rideId, String fromUserId, String toUserId, int score, String comment) {
        this.rideId = rideId;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.score = score;
        this.comment = comment;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        if (score < 1 || score > 5) {
            throw new IllegalArgumentException("Rating score must be between 1 and 5");
        }
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
} 