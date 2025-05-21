package com.example.ridesharing.controller;

import com.example.ridesharing.service.RideService;
import com.example.ridesharing.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;
import com.example.ridesharing.model.Ride;
import com.example.ridesharing.enums.RideStatus;
import java.time.LocalDateTime;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RideControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RideService rideService;

    @MockBean
    private UserRepository userRepository;

    @Test
    public void testGetRideById_NotFound() throws Exception {
        // Simulate authenticated user with correct role
        // This endpoint requires authentication, so add @WithMockUser
        when(rideService.getRideById("ride123")).thenReturn(java.util.Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.get("/api/rides/ride123")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("student1").roles("STUDENT")))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateRide_Unauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/rides"))
                .andExpect(status().isForbidden()); // Most secured endpoints return 403 Forbidden if not authenticated/authorized
    }

    @Test
    public void testGetRideById_Forbidden() throws Exception {
        // Simulate forbidden access (e.g., missing role)
        when(rideService.getRideById("ride123")).thenReturn(java.util.Optional.of(new com.example.ridesharing.model.Ride()));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/rides/ride123"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testBookRide_Unauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/rides/ride123/book"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCancelRide_Unauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/rides/ride123/cancel"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testUnbookRide_Unauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/rides/ride123/unbook"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testSearchRides_Unauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/rides/search"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"DRIVER", "STUDENT"})
    public void testGetRideById_Found() throws Exception {
        when(rideService.getRideById("ride123")).thenReturn(java.util.Optional.of(new com.example.ridesharing.model.Ride()));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/rides/ride123"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    public void testBookRide_Authorized() throws Exception {
        when(rideService.bookRide(anyString(), anyString())).thenReturn(new Ride());
        mockMvc.perform(MockMvcRequestBuilders.post("/api/rides/ride123/book"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "driver1", roles = {"DRIVER"})
    public void testCancelRide_Authorized() throws Exception {
        Ride ride = new Ride();
        ride.setDriverUsername("driver1");
        when(rideService.getRideById(anyString())).thenReturn(java.util.Optional.of(ride));
        when(rideService.updateRideStatus(anyString(), anyString(), any())).thenReturn(ride);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/rides/ride123/cancel"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    public void testUnbookRide_Authorized() throws Exception {
        doNothing().when(rideService).cancelBooking(anyString(), anyString());
        mockMvc.perform(MockMvcRequestBuilders.post("/api/rides/ride123/unbook"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"STUDENT", "DRIVER"})
    public void testSearchRides_Authorized() throws Exception {
        when(rideService.searchRides(any(), any(), any(), any(), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any())).thenReturn(org.springframework.data.domain.Page.empty());
        mockMvc.perform(MockMvcRequestBuilders.get("/api/rides/search"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "driver1", roles = {"DRIVER"})
    public void testCreateRide_Authorized() throws Exception {
        Ride ride = new Ride();
        ride.setOrigin("A");
        ride.setDestination("B");
        ride.setDepartureTime(LocalDateTime.now().plusDays(1));
        ride.setAvailableSeats(2);
        ride.setMaxPassengers(2);
        ride.setStatus(RideStatus.SCHEDULED);
        ride.setDriverUsername("driver1");
        when(rideService.createRide(any(Ride.class))).thenReturn(ride);
        String rideJson = "{" +
                "\"origin\":\"A\"," +
                "\"destination\":\"B\"," +
                "\"departureTime\":\"2025-12-31T12:00:00\"," +
                "\"availableSeats\":2," +
                "\"maxPassengers\":2," +
                "\"price\":0.0" +
                "}";
        mockMvc.perform(MockMvcRequestBuilders.post("/api/rides")
                .contentType(MediaType.APPLICATION_JSON)
                .content(rideJson))
                .andExpect(status().isOk());
    }

    // Add more tests as needed
}
