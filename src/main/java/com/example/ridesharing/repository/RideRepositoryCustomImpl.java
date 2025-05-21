package com.example.ridesharing.repository;

import com.example.ridesharing.enums.RideStatus;
import com.example.ridesharing.model.Ride;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class RideRepositoryCustomImpl implements RideRepositoryCustom {

    private static final Logger logger = LoggerFactory.getLogger(RideRepositoryCustomImpl.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<Ride> searchRides(
            String origin,
            String destination,
            LocalDateTime fromDepartureTime,
            LocalDateTime toDepartureTime,
            Double maxPrice,
            Integer minSeats,
            Boolean includeFullRides,
            List<RideStatus> statuses
    ) {
        logger.debug("Searching rides with filters: origin={}, destination={}, fromTime={}, toTime={}, maxPrice={}, minSeats={}, includeFullRides={}, statuses={}",
                origin, destination, fromDepartureTime, toDepartureTime, maxPrice, minSeats, includeFullRides, statuses);
        
        Query query = new Query();
        
        // Origin filter (case-insensitive partial match)
        if (origin != null && !origin.trim().isEmpty()) {
            query.addCriteria(Criteria.where("origin").regex(origin, "i"));
            logger.debug("Added origin filter: {}", origin);
        }
        
        // Destination filter (case-insensitive partial match)
        if (destination != null && !destination.trim().isEmpty()) {
            query.addCriteria(Criteria.where("destination").regex(destination, "i"));
            logger.debug("Added destination filter: {}", destination);
        }
        
        // Departure time range filter
        if (fromDepartureTime != null || toDepartureTime != null) {
            Criteria departureTimeCriteria = Criteria.where("departureTime");
            if (fromDepartureTime != null) {
                departureTimeCriteria.gte(fromDepartureTime);
                logger.debug("Added from time filter: {}", fromDepartureTime);
            }
            if (toDepartureTime != null) {
                departureTimeCriteria.lte(toDepartureTime);
                logger.debug("Added to time filter: {}", toDepartureTime);
            }
            query.addCriteria(departureTimeCriteria);
        }
        
        // Price filter
        if (maxPrice != null) {
            query.addCriteria(Criteria.where("price").lte(maxPrice));
            logger.debug("Added max price filter: {}", maxPrice);
        }
        
        // Available seats filter
        if (!Boolean.TRUE.equals(includeFullRides)) {
            query.addCriteria(Criteria.where("availableSeats").gt(0));
            logger.debug("Filtering out full rides");
        }
        if (minSeats != null) {
            query.addCriteria(Criteria.where("availableSeats").gte(minSeats));
            logger.debug("Added minimum seats filter: {}", minSeats);
        }
        
        // Status filter
        if (statuses != null && !statuses.isEmpty()) {
            query.addCriteria(Criteria.where("status").in(statuses));
            logger.debug("Added status filter: {}", statuses);
        } else {
            // By default, only show SCHEDULED rides and exclude CANCELLED ones
            query.addCriteria(Criteria.where("status").is(RideStatus.SCHEDULED));
            logger.debug("Added default status filter: SCHEDULED only");
        }
        
        // Sort by departure time ascending by default
        query.with(Sort.by(Sort.Direction.ASC, "departureTime"));
        
        List<Ride> results = mongoTemplate.find(query, Ride.class);
        logger.debug("Found {} rides matching criteria", results.size());
        
        return results;
    }
}