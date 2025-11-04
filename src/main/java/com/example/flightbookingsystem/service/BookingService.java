package com.example.flightbookingsystem.service;


import com.example.flightbookingsystem.dto.BookingDTO;
import com.example.flightbookingsystem.exception.BusinessException;
import com.example.flightbookingsystem.exception.ResourceNotFoundException;
import com.example.flightbookingsystem.model.Booking;
import com.example.flightbookingsystem.model.Flight;
import com.example.flightbookingsystem.model.User;
import com.example.flightbookingsystem.repository.BookingRepository;
import com.example.flightbookingsystem.repository.FlightRepository;
import com.example.flightbookingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {
    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final UserRepository userRepository;
    private final FlightService flightService;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Transactional
    public BookingDTO createBooking(BookingDTO bookingDTO) {
        log.info("Creating booking for flight: {}", bookingDTO.getFlightId());

        Flight flight = flightRepository.findById(bookingDTO.getFlightId())
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));

        User user = userRepository.findById(bookingDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (flight.getAvailableSeats() < bookingDTO.getNumberOfSeats()) {
            throw new BusinessException("Not enough seats available");
        }

        if (!flightService.decreaseAvailableSeats(flight.getId(), bookingDTO.getNumberOfSeats())) {
            throw new BusinessException("Failed to book seats");
        }

        Booking booking = Booking.builder()
                .bookingReference(generateBookingReference())
                .user(user)
                .flight(flight)
                .passengerFirstName(bookingDTO.getPassengerFirstName())
                .passengerLastName(bookingDTO.getPassengerLastName())
                .passengerEmail(bookingDTO.getPassengerEmail())
                .passengerPhone(bookingDTO.getPassengerPhone())
                .numberOfSeats(bookingDTO.getNumberOfSeats())
                .totalPrice(flight.getPrice().multiply(BigDecimal.valueOf(bookingDTO.getNumberOfSeats())))
                .status(Booking.BookingStatus.CONFIRMED)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created successfully: {}", savedBooking.getBookingReference());

        return convertToDTO(savedBooking);
    }

    @Cacheable(value = "bookings", key = "'ref:' + #reference", unless = "#result == null")
    public BookingDTO getBookingByReference(String reference) {
        log.info("Fetching booking with reference: {} from database", reference);
        Booking booking = bookingRepository.findByBookingReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        return convertToDTO(booking);
    }

    @Cacheable(value = "userBookings", key = "#userId", unless = "#result == null || #result.isEmpty()")
    public List<BookingDTO> getUserBookings(Long userId) {
        log.info("Fetching bookings for user: {} from database", userId);
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        return bookings.stream().map(this::convertToDTO).toList();
    }

    @Transactional
    public void cancelBooking(String bookingReference) {
        log.info("Cancelling booking: {}", bookingReference);
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new BusinessException("Booking already cancelled");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        Flight flight = booking.getFlight();
        flight.setAvailableSeats(flight.getAvailableSeats() + booking.getNumberOfSeats());
        flightRepository.save(flight);
    }

    private String generateBookingReference() {
        return "BK" + System.currentTimeMillis() + SECURE_RANDOM.nextInt(1000);
    }

    private BookingDTO convertToDTO(Booking booking) {
        return BookingDTO.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .userId(booking.getUser().getId())
                .flightId(booking.getFlight().getId())
                .flightNumber(booking.getFlight().getFlightNumber())
                .passengerFirstName(booking.getPassengerFirstName())
                .passengerLastName(booking.getPassengerLastName())
                .passengerEmail(booking.getPassengerEmail())
                .passengerPhone(booking.getPassengerPhone())
                .numberOfSeats(booking.getNumberOfSeats())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus().name())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}

