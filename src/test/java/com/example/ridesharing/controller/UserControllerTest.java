package com.example.ridesharing.controller;

import com.example.ridesharing.service.RatingService;
import com.example.ridesharing.repository.UserRepository;
import com.example.ridesharing.repository.RideRepository;
import com.example.ridesharing.service.NotificationService;
import com.example.ridesharing.repository.NotificationRepository;
import com.example.ridesharing.repository.NotificationPreferenceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private RatingService ratingService;
    @MockBean
    private RideRepository rideRepository;
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationRepository notificationRepository;
    @MockBean
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @Test
    public void testGetCurrentUser_Unauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetUserProfile_NotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/nonexistent"))
                .andExpect(status().isUnauthorized());
    }

    // Add more endpoint tests as needed
}
