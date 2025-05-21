package com.example.ridesharing.repository;

import com.example.ridesharing.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findByStudentId(String studentId);
    List<Booking> findByRideId(String rideId);
    long countByStudentId(String studentId);
}
