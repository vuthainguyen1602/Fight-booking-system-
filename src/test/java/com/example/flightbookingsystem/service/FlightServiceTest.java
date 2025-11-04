package com.example.flightbookingsystem.service;

import com.example.flightbookingsystem.dto.FlightDTO;
import com.example.flightbookingsystem.exception.ResourceNotFoundException;
import com.example.flightbookingsystem.model.Flight;
import com.example.flightbookingsystem.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Alternative test configuration using Spring Boot Test context
 * Use this if FlightService has Spring-specific dependencies or caching annotations
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
   "spring.cache.type=none"
})
@DisplayName("Flight Service Unit Tests - Spring Context")
class FlightServiceSpringTest {

    @MockBean
    private FlightRepository flightRepository;

    @Autowired(required = false)
    private CacheManager cacheManager;

    @Autowired
    private FlightService flightService;

    private Flight testFlight;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;

    @BeforeEach
    void setUp() {
        // Clear all caches before each test
        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(cacheName -> {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                }
            });
        }

        // Reset mocks
        reset(flightRepository);

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
    @DisplayName("Should throw exception when flight not found")
    void getFlightById_ShouldThrowException_WhenFlightNotFound() {
        when(flightRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> flightService.getFlightById(999L));
        verify(flightRepository, times(1)).findById(999L);
    }

}