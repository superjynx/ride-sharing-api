package com.example.ridesharing.controller;

import com.example.ridesharing.service.RatingService;
import com.example.ridesharing.repository.UserRepository;
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

@WebMvcTest(RatingController.class)
public class RatingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RatingService ratingService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationRepository notificationRepository;

    @MockBean
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @Test
    public void testSubmitRating_Unauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/ratings/rides/ride123").param("toUserId", "user2"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetRideRatings_Unauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/ratings/rides/ride123"))
                .andExpect(status().isUnauthorized());
    }

    // Add more endpoint tests as needed
}
