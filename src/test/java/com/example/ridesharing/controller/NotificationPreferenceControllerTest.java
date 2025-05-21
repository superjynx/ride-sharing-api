package com.example.ridesharing.controller;

import com.example.ridesharing.service.NotificationPreferenceService;
import com.example.ridesharing.repository.NotificationPreferenceRepository;
import com.example.ridesharing.service.NotificationService;
import com.example.ridesharing.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationPreferenceController.class)
public class NotificationPreferenceControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationPreferenceService preferenceService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationRepository notificationRepository;

    @MockBean
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @Test
    public void testGetPreferences_Unauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/notification-preferences"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdatePreferences_Unauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/notification-preferences"))
                .andExpect(status().isUnauthorized());
    }

    // Add more endpoint tests as needed
}
