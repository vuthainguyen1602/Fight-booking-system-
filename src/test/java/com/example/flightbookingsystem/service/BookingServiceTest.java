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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Booking Service Unit Tests")
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private FlightRepository  flightRepository;
    @Mock private UserRepository    userRepository;
    @Mock private FlightService     flightService;

    private BookingService bookingService;

    private Flight testFlight;
    private User   testUser;
    private BookingDTO testBookingDTO;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(bookingRepository,
                flightRepository,
                userRepository,
                flightService);

        testFlight = Flight.builder()
                .id(1L).flightNumber("VN123").airline("Vietnam Airlines")
                .origin("SGN").destination("HAN")
                .departureTime(LocalDateTime.now().plusDays(1))
                .arrivalTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .totalSeats(200).availableSeats(50)
                .price(new BigDecimal("1500000"))
                .status(Flight.FlightStatus.SCHEDULED)
                .build();

        testUser = User.builder()
                .id(1L).email("test@example.com").auth0Id("auth0|123456")
                .firstName("John").lastName("Doe")
                .role(User.Role.USER).active(true)
                .build();

        testBookingDTO = BookingDTO.builder()
                .userId(1L).flightId(1L)
                .passengerFirstName("John").passengerLastName("Doe")
                .passengerEmail("john@example.com")
                .passengerPhone("+84901234567")
                .numberOfSeats(2)
                .build();
    }

    private Booking createConfirmedBooking() {
        return Booking.builder()
                .id(1L).bookingReference("BK1234567890123")
                .user(testUser).flight(testFlight)
                .passengerFirstName("John").passengerLastName("Doe")
                .passengerEmail("john@example.com")
                .passengerPhone("+84901234567")
                .numberOfSeats(2)
                .totalPrice(new BigDecimal("3000000"))
                .status(Booking.BookingStatus.CONFIRMED)
                .build();
    }

    @Test
    void createBooking_ShouldCreateSuccessfully() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(testFlight));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(flightService.decreaseAvailableSeats(1L, 2)).thenReturn(true);

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        when(bookingRepository.save(captor.capture())).thenAnswer(i -> {
            Booking b = i.getArgument(0);
            b.setId(1L);
            b.setBookingReference("BK1234567890123");
            return b;
        });

        BookingDTO result = bookingService.createBooking(testBookingDTO);

        assertNotNull(result);
        assertEquals("BK1234567890123", result.getBookingReference());

        Booking saved = captor.getValue();
        assertEquals(new BigDecimal("3000000"), saved.getTotalPrice());

        verify(flightService).decreaseAvailableSeats(1L, 2);
    }

    @Test
    void createBooking_ShouldThrowException_WhenFlightNotFound() {
        when(flightRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.createBooking(testBookingDTO));

        verify(flightRepository).findById(1L);
        verifyNoInteractions(userRepository, bookingRepository);
    }

    @Test
    void createBooking_ShouldThrowException_WhenNotEnoughSeats() {
        Flight limited = Flight.builder()
                .id(1L).availableSeats(1) /* copy other fields from testFlight */
                .flightNumber("VN123").airline("Vietnam Airlines")
                .origin("SGN").destination("HAN")
                .departureTime(LocalDateTime.now().plusDays(1))
                .arrivalTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .totalSeats(200).price(new BigDecimal("1500000"))
                .status(Flight.FlightStatus.SCHEDULED)
                .build();

        when(flightRepository.findById(1L)).thenReturn(Optional.of(limited));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> bookingService.createBooking(testBookingDTO));
        assertTrue(ex.getMessage().contains("Not enough seats"));
    }

    @Test
    void cancelBooking_ShouldCancelSuccessfully() {
        Booking confirmed = createConfirmedBooking(); // fresh copy
        when(bookingRepository.findByBookingReference("BK1234567890123"))
                .thenReturn(Optional.of(confirmed));
        when(bookingRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(flightRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> bookingService.cancelBooking("BK1234567890123"));

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        assertEquals(Booking.BookingStatus.CANCELLED, captor.getValue().getStatus());
    }

    @Test
    void getUserBookings_ShouldReturnBookingList() {
        Booking b1 = createConfirmedBooking();
        Booking b2 = createConfirmedBooking();
        b2.setId(2L);
        b2.setBookingReference("BK9876543210987");

        when(bookingRepository.findByUserId(1L)).thenReturn(List.of(b1, b2));

        List<BookingDTO> result = bookingService.getUserBookings(1L);
        assertEquals(2, result.size());
    }
}