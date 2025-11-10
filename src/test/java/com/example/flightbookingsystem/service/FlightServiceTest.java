package com.example.flightbookingsystem.service;

import com.example.flightbookingsystem.dto.FlightDTO;
import com.example.flightbookingsystem.exception.ResourceNotFoundException;
import com.example.flightbookingsystem.model.Flight;
import com.example.flightbookingsystem.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private FlightService flightService;

    private Flight testFlight;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;

    @BeforeEach
    void setUp() {
        departureTime = LocalDateTime.now().plusDays(1);
        arrivalTime = departureTime.plusHours(2);

        testFlight = Flight.builder()
                .id(1L)
                .flightNumber("VN123")
                .airline("Vietnam Airlines")
                .origin("SGN")
                .destination("HAN")
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .totalSeats(200)
                .availableSeats(150)
                .price(new BigDecimal("1500000"))
                .status(Flight.FlightStatus.SCHEDULED)
                .build();
    }

    @Test
    @DisplayName("Should return flight when flight exists")
    void getFlightById_ShouldReturnFlight_WhenFlightExists() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(testFlight));

        FlightDTO result = flightService.getFlightById(1L);

        assertNotNull(result);
        assertEquals("VN123", result.getFlightNumber());
        verify(flightRepository, times(1)).findById(1L);
    }

    @Test
    void getFlightById_ShouldThrowException_WhenFlightNotFound() {
        Long id = 999L;
        assertThrows(ResourceNotFoundException.class, () -> {
            flightService.getFlightById(id);
        });
    }
}
