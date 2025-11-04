package com.example.flightbookingsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.flightbookingsystem.model.Booking;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingReference(String bookingReference);

    List<Booking> findByUserId(Long userId);

    List<Booking> findByFlightId(Long flightId);
}
