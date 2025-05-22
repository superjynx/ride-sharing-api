package com.example.ridesharing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testUserRegistrationLoginAndProtectedEndpoint() throws Exception {
        // 1. Register a new user
        String username = "testuser123";
        String password = "testpass123";
        String role = "ROLE_STUDENT";
        var signupPayload = Map.of(
                "username", username,
                "password", password,
                "role", role
        );
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupPayload)))
                .andExpect(status().isOk());

        // 2. Login with the new user
        var loginPayload = Map.of(
                "username", username,
                "password", password
        );
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isOk())
                .andReturn();
        String loginResponse = loginResult.getResponse().getContentAsString();
        System.out.println("LOGIN RESPONSE: " + loginResponse); // Debug print
        Map<?,?> loginMap = objectMapper.readValue(loginResponse, Map.class);
        String token = (String) loginMap.get("token");
        assertThat(token).isNotBlank();

        // 3. Access a protected endpoint with JWT
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
