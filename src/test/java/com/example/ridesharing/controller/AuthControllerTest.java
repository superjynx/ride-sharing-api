package com.example.ridesharing.controller;

import com.example.ridesharing.repository.UserRepository;
import com.example.ridesharing.util.JwtUtils;
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

@WebMvcTest(AuthController.class)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private JwtUtils jwtUtils;
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationRepository notificationRepository;
    @MockBean
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @Test
    public void testLogin_Unauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testSignup_BadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup"))
                .andExpect(status().isBadRequest());
    }

    // Add more endpoint tests as needed
}
