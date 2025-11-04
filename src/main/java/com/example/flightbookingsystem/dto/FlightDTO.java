package com.example.flightbookingsystem.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightDTO {
    private Long id;

    @NotBlank(message = "Flight number is required")
    @Size(max = 10, message = "Flight number must not exceed 10 characters")
    private String flightNumber;

    @NotBlank(message = "Airline is required")
    @Size(max = 100, message = "Airline must not exceed 100 characters")
    private String airline;

    @NotBlank(message = "Origin is required")
    @Size(min = 3, max = 3, message = "Origin must be 3 characters")
    private String origin;

    @NotBlank(message = "Destination is required")
    @Size(min = 3, max = 3, message = "Destination must be 3 characters")
    private String destination;

    @NotNull(message = "Departure time is required")
    @Future(message = "Departure time must be in the future")
    private LocalDateTime departureTime;

    @NotNull(message = "Arrival time is required")
    private LocalDateTime arrivalTime;

    @NotNull(message = "Total seats is required")
    @Min(value = 1, message = "Total seats must be at least 1")
    private Integer totalSeats;

    @NotNull(message = "Available seats is required")
    @Min(value = 0, message = "Available seats cannot be negative")
    private Integer availableSeats;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    private String status;
}

