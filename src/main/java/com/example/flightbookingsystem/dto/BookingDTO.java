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
public class BookingDTO {
    private Long id;
    private String bookingReference;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Flight ID is required")
    private Long flightId;

    private String flightNumber;

    @NotBlank(message = "Passenger first name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String passengerFirstName;

    @NotBlank(message = "Passenger last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String passengerLastName;

    @NotBlank(message = "Passenger email is required")
    @Email(message = "Invalid email format")
    private String passengerEmail;

    @NotBlank(message = "Passenger phone is required")
    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "Invalid phone number format")
    private String passengerPhone;

    @NotNull(message = "Number of seats is required")
    @Min(value = 1, message = "At least 1 seat must be booked")
    @Max(value = 9, message = "Maximum 9 seats per booking")
    private Integer numberOfSeats;

    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime createdAt;
}