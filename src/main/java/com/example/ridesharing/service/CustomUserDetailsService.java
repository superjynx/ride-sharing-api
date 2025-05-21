package com.example.ridesharing.service;

import com.example.ridesharing.exception.ResourceNotFoundException;
import com.example.ridesharing.model.User;
import com.example.ridesharing.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find the user by username from the database
        User user = userRepository.findByUsername(username);

        // If user is not found, throw ResourceNotFoundException
        if (user == null) {
            logger.error("User not found: {}", username);
            throw new ResourceNotFoundException("User not found");
        }

        // Log the loaded user details
        logger.debug("Loading user: {}, role: {}", user.getUsername(), user.getRole());

        // Create and return UserDetails with proper authority
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }
}
