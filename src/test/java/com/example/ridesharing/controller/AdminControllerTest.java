package com.example.ridesharing.controller;

import com.example.ridesharing.repository.UserRepository;
import com.example.ridesharing.repository.RideRepository;
import com.example.ridesharing.repository.BookingRepository;
import com.example.ridesharing.repository.NotificationRepository;
import com.example.ridesharing.repository.NotificationPreferenceRepository;
import com.example.ridesharing.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
public class AdminControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private RideRepository rideRepository;
    @MockBean
    private BookingRepository bookingRepository;
    @MockBean
    private NotificationRepository notificationRepository;
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @Test
    public void testGetAllUsers_Unauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetAllRides_Unauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/rides"))
                .andExpect(status().isUnauthorized());
    }

    // Add more endpoint tests as needed
}
