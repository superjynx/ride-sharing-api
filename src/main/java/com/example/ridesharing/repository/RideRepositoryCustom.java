package com.example.ridesharing.repository;

import com.example.ridesharing.model.Ride;
import com.example.ridesharing.enums.RideStatus;
import java.time.LocalDateTime;
import java.util.List;

public interface RideRepositoryCustom {
    List<Ride> searchRides(
        String origin,
        String destination,
        LocalDateTime fromDepartureTime,
        LocalDateTime toDepartureTime,
        Double maxPrice,
        Integer minSeats,
        Boolean includeFullRides,
        List<RideStatus> statuses
    );
}