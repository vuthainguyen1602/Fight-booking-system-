package com.example.flightbookingsystem.repository;

import com.example.flightbookingsystem.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    Optional<Flight> findByFlightNumber(String flightNumber);

    List<Flight> findByOriginAndDestinationAndDepartureTimeBetween(
            String origin,
            String destination,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    @Query("SELECT f FROM Flight f WHERE f.origin = :origin AND f.destination = :destination "
            + "AND f.departureTime >= :departureTime AND f.availableSeats >= :seats "
            + "AND f.status = 'SCHEDULED'")
    List<Flight> searchAvailableFlights(
            String origin,
            String destination,
            LocalDateTime departureTime,
            Integer seats
    );
}
