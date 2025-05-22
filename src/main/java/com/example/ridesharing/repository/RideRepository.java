package com.example.ridesharing.repository;

import com.example.ridesharing.model.Ride;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RideRepository extends MongoRepository<Ride, String>, RideRepositoryCustom {
    List<Ride> findByDriverUsername(String driverUsername);
    Page<Ride> findByDriverUsername(String driverUsername, Pageable pageable);
}
