package com.example.flightbookingsystem.flight;

import com.example.flightbookingsystem.dto.FlightDTO;
import com.example.flightbookingsystem.model.Flight;
import com.example.flightbookingsystem.repository.FlightRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FlightControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FlightRepository flightRepository;

    private Flight testFlight;

    @BeforeEach
    void setUp() {
        flightRepository.deleteAll();

        testFlight = Flight.builder()
                .flightNumber("VN123")
                .airline("Vietnam Airlines")
                .origin("SGN")
                .destination("HAN")
                .departureTime(LocalDateTime.now().plusDays(1))
                .arrivalTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .totalSeats(200)
                .availableSeats(150)
                .price(new BigDecimal("1500000"))
                .status(Flight.FlightStatus.SCHEDULED)
                .build();

        flightRepository.save(testFlight);
    }

    @Test
    void getFlightById_ShouldReturnFlight() throws Exception {
        mockMvc.perform(get("/api/v1/flights/{id}", testFlight.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("VN123"))
                .andExpect(jsonPath("$.airline").value("Vietnam Airlines"));
    }

    @Test
    void getFlightById_ShouldReturn404_WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/flights/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFlightByNumber_ShouldReturnFlight() throws Exception {
        mockMvc.perform(get("/api/v1/flights/number/{flightNumber}", "VN123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("VN123"));
    }

    @Test
    void searchFlights_ShouldReturnFlightList() throws Exception {
        String requestBody = """
            {
                "origin": "SGN",
                "destination": "HAN",
                "departureTime": "%s",
                "seats": 2
            }
            """.formatted(LocalDateTime.now().plusDays(1).toString());

        mockMvc.perform(post("/api/v1/flights/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "SCOPE_admin")
    void createFlight_ShouldReturnCreatedFlight() throws Exception {
        FlightDTO flightDTO = FlightDTO.builder()
                .flightNumber("VN456")
                .airline("Bamboo Airways")
                .origin("HAN")
                .destination("SGN")
                .departureTime(LocalDateTime.now().plusDays(2))
                .arrivalTime(LocalDateTime.now().plusDays(2).plusHours(2))
                .totalSeats(180)
                .availableSeats(180)
                .price(new BigDecimal("1200000"))
                .status("SCHEDULED")
                .build();

        mockMvc.perform(post("/api/v1/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flightDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flightNumber").value("VN456"))
                .andExpect(jsonPath("$.airline").value("Bamboo Airways"));
    }
}
