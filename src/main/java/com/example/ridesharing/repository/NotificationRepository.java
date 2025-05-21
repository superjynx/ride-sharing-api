package com.example.ridesharing.repository;

import com.example.ridesharing.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    Page<Notification> findByUserIdOrderByTimestampDesc(String userId, Pageable pageable);
    List<Notification> findByUserIdAndReadFalse(String userId);
    long countByUserIdAndReadFalse(String userId);
}
