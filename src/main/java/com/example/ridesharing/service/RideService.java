package com.example.ridesharing.service;

import com.example.ridesharing.enums.RideStatus;
import com.example.ridesharing.model.Ride;
import com.example.ridesharing.repository.RideRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.example.ridesharing.exception.ResourceNotFoundException;
import com.example.ridesharing.exception.BadRequestException;
import com.example.ridesharing.exception.ConflictException;
import com.example.ridesharing.exception.UnauthorizedException;

@Service
public class RideService {    private static final Logger logger = LoggerFactory.getLogger(RideService.class);
    private final RideRepository rideRepository;
    private final MongoTemplate mongoTemplate;
    private final NotificationService notificationService;

    public RideService(RideRepository rideRepository, MongoTemplate mongoTemplate, NotificationService notificationService) {
        this.rideRepository = rideRepository;
        this.mongoTemplate = mongoTemplate;
        this.notificationService = notificationService;
    }

    /**
     * Create a new ride
     */
    public Ride createRide(Ride ride) {
        logger.debug("Creating new ride: {}", ride);
        if (ride.getOrigin() == null || ride.getDestination() == null || 
            ride.getDepartureTime() == null || ride.getAvailableSeats() < 1 || ride.getMaxPassengers() < 1) {
            throw new BadRequestException("Missing required fields or invalid values");
        }
        return rideRepository.save(ride);
    }

    /**
     * Search rides with pagination and date filtering
     */
    public Page<Ride> searchRides(
        String origin,
        String destination,
        LocalDateTime from,
        LocalDateTime to,
        Double maxPrice,
        Integer minSeats,
        Boolean includeFull,
        List<RideStatus> statuses,
        boolean includePastRides,
        int page,
        int size,
        String sortBy,
        String sortDir
) {
    Criteria criteria = new Criteria();

    List<Criteria> filters = new ArrayList<>();

    if (origin != null && !origin.isEmpty()) {
        filters.add(Criteria.where("origin").regex(origin, "i"));
    }

    if (destination != null && !destination.isEmpty()) {
        filters.add(Criteria.where("destination").regex(destination, "i"));
    }

    if (!includePastRides) {
        filters.add(Criteria.where("departureTime").gte(LocalDateTime.now()));
    }

    if (from != null) {
        filters.add(Criteria.where("departureTime").gte(from));
    }

    if (to != null) {
        filters.add(Criteria.where("departureTime").lte(to));
    }

    if (maxPrice != null) {
        filters.add(Criteria.where("price").lte(maxPrice));
    }

    // ✅ Combine availableSeats conditions into one
    if (minSeats != null && !includeFull) {
        filters.add(Criteria.where("availableSeats").gte(minSeats));
    } else if (minSeats != null) {
        filters.add(Criteria.where("availableSeats").gte(minSeats));
    } else if (!includeFull) {
        filters.add(Criteria.where("availableSeats").gt(0));
    }

    if (statuses != null && !statuses.isEmpty()) {
        filters.add(Criteria.where("status").in(statuses));
    }

    if (!filters.isEmpty()) {
        criteria.andOperator(filters.toArray(new Criteria[0]));
    }

    Query query = new Query(criteria);

    // Pagination
    Pageable pageable = PageRequest.of(page, size);
    query.with(pageable);

    // Sorting
    if (sortBy != null && sortDir != null) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        query.with(Sort.by(direction, sortBy));
    }

    logger.debug("Search filters applied: {}", query);

    List<Ride> rides = mongoTemplate.find(query, Ride.class);
    long count = mongoTemplate.count(query.skip(-1).limit(-1), Ride.class); // Count without pagination

    return new PageImpl<>(rides, pageable, count);
}


    /**
     * Cancel a booking for a student.
     */
    public void cancelBooking(String rideId, String username) {
        logger.debug("Cancel booking request for rideId: {}, username: {}", rideId, username);

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found"));

        if (!ride.getPassengers().contains(username)) {
            throw new ConflictException("You have not booked this ride");
        }

        // Remove passenger and increase available seats
        ride.getPassengers().remove(username);
        ride.setAvailableSeats(ride.getAvailableSeats() + 1);

        Ride updatedRide = rideRepository.save(ride);
        notificationService.notifyRideStatusChange(updatedRide);

        logger.info("Booking cancelled for user {} on ride {}", username, rideId);
    }

    /**
     * Get a ride by its ID.
     */
    public Optional<Ride> getRideById(String rideId) {
        logger.debug("Fetching ride with ID: {}", rideId);
        return rideRepository.findById(rideId);
    }

    /**
     * Update the status of a ride, with driver validation and status transition validation.
     */
    public Ride updateRideStatus(String rideId, String driverUsername, RideStatus newStatus) {
        logger.debug("Updating ride {} status to {} by driver {}", rideId, newStatus, driverUsername);

        Optional<Ride> optionalRide = rideRepository.findById(rideId);
        if (optionalRide.isEmpty()) {
            throw new ResourceNotFoundException("Ride not found");
        }

        Ride ride = optionalRide.get();

        // Verify the driver owns this ride
        if (ride.getDriverUsername() == null || !ride.getDriverUsername().equals(driverUsername)) {
            throw new UnauthorizedException("You are not the driver of this ride.");
        }

        // Check valid status transition
        if (!isValidStatusTransition(ride.getStatus(), newStatus)) {
            throw new BadRequestException("Invalid status transition from " + ride.getStatus() + " to " + newStatus);
        }

        ride.setStatus(newStatus);
        Ride savedRide = rideRepository.save(ride);

        // Send notifications for status change
        notificationService.notifyRideStatusChange(savedRide);

        logger.info("Ride {} status updated to {} by driver {}", rideId, newStatus, driverUsername);

        // If status is IN_PROGRESS, send departure notifications
        if (newStatus == RideStatus.IN_PROGRESS) {
            notificationService.sendDepartureReminder(savedRide);
        }

        return savedRide;
    }

    /**
     * Book a ride with date validation
     */
    public Ride bookRide(String rideId, String studentUsername) {
        logger.debug("Booking ride {} for student {}", rideId, studentUsername);
        
        Ride ride = getRideById(rideId)
            .orElseThrow(() -> new ResourceNotFoundException("Ride not found"));

        // Validate ride status
        if (ride.getStatus() != RideStatus.SCHEDULED) {
            throw new ConflictException("This ride cannot be booked (status: " + ride.getStatus() + ")");
        }

        // Validate departure time
        if (ride.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new ConflictException("Cannot book a ride that has already departed");
        }

        // Check available seats
        if (ride.getAvailableSeats() <= 0) {
            throw new ConflictException("No seats available");
        }

        // Check if student has already booked
        if (ride.getPassengers().contains(studentUsername)) {
            throw new ConflictException("You have already booked this ride");
        }

        // Enforce maxPassengers
        if (ride.getMaxPassengers() > 0 && ride.getPassengers().size() >= ride.getMaxPassengers()) {
            throw new ConflictException("Ride is full");
        }

        // Add passenger and update seats
        ride.getPassengers().add(studentUsername);
        ride.setAvailableSeats(ride.getAvailableSeats() - 1);

        Ride savedRide = rideRepository.save(ride);
        
        // Send booking notifications
        notificationService.notifyRideBooked(savedRide, studentUsername);

        return savedRide;
    }

    /**
     * Get rides associated with a user (as driver or passenger)
     */
    public Page<Ride> getUserRides(String username, boolean asDriver, Pageable pageable) {
        logger.debug("Fetching rides for user {} as {}", username, asDriver ? "driver" : "passenger");
        
        Query query = new Query().with(pageable);
        
        if (asDriver) {
            query.addCriteria(Criteria.where("driverUsername").is(username));
        } else {
            query.addCriteria(Criteria.where("passengers").in(username));
        }
        
        // Sort by departure time descending by default
        if (pageable.getSort().isUnsorted()) {
            query.with(Sort.by(Sort.Direction.DESC, "departureTime"));
        }
        
        long total = mongoTemplate.count(query, Ride.class);
        List<Ride> rides = mongoTemplate.find(query, Ride.class);
        
        return new PageImpl<>(rides, pageable, total);
    }

    // Get rides for a user (driver or passenger) with status filtering
    public Page<Ride> getUserRidesWithStatus(String username, boolean asDriver, List<RideStatus> statuses, Pageable pageable) {
        logger.debug("Fetching rides for user {} as {} with statuses {}", username, asDriver ? "driver" : "passenger", statuses);
        Query query = new Query().with(pageable);
        if (asDriver) {
            query.addCriteria(Criteria.where("driverUsername").is(username));
        } else {
            query.addCriteria(Criteria.where("passengers").in(username));
        }
        if (statuses != null && !statuses.isEmpty()) {
            query.addCriteria(Criteria.where("status").in(statuses));
        }
        if (pageable.getSort().isUnsorted()) {
            query.with(Sort.by(Sort.Direction.DESC, "departureTime"));
        }
        long total = mongoTemplate.count(query, Ride.class);
        List<Ride> rides = mongoTemplate.find(query, Ride.class);
        return new PageImpl<>(rides, pageable, total);
    }

    private boolean isValidStatusTransition(RideStatus currentStatus, RideStatus newStatus) {
        if (currentStatus == newStatus) {
            return true; // Allow same status set
        }

        switch (currentStatus) {
            case SCHEDULED:
                return newStatus == RideStatus.IN_PROGRESS || newStatus == RideStatus.CANCELLED;
            case IN_PROGRESS:
                return newStatus == RideStatus.COMPLETED;
            case COMPLETED:
            case CANCELLED:
                return false;
            default:
                return false;
        }
    }

    public Set<String> getPassengersByRideId(String rideId, Authentication auth) {
    Ride ride = rideRepository.findById(rideId)
        .orElseThrow(() -> new ResourceNotFoundException("Ride not found"));

    String driverUsername = auth.getName();
    if (!ride.getDriverUsername().equals(driverUsername)) {
        throw new UnauthorizedException("You are not the driver of this ride");
    }

    return ride.getPassengers();
}

// Driver removes a specific passenger from their ride
    public Ride removePassengerByDriver(String rideId, String driverUsername, String passengerUsername) {
        logger.debug("Driver {} requests to remove passenger {} from ride {}", driverUsername, passengerUsername, rideId);
        Ride ride = rideRepository.findById(rideId)
            .orElseThrow(() -> new ResourceNotFoundException("Ride not found"));
        if (!ride.getDriverUsername().equals(driverUsername)) {
            throw new UnauthorizedException("You are not the driver of this ride");
        }
        if (!ride.getPassengers().contains(passengerUsername)) {
            throw new BadRequestException("Passenger is not booked on this ride");
        }
        ride.getPassengers().remove(passengerUsername);
        ride.setAvailableSeats(ride.getAvailableSeats() + 1);
        Ride updatedRide = rideRepository.save(ride);
        notificationService.notifyRideStatusChange(updatedRide);
        logger.info("Passenger {} removed from ride {} by driver {}", passengerUsername, rideId, driverUsername);
        return updatedRide;
    }

}
