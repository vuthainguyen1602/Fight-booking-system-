package com.example.flightbookingsystem.service;


import com.example.flightbookingsystem.dto.FlightDTO;
import com.example.flightbookingsystem.exception.ResourceNotFoundException;
import com.example.flightbookingsystem.model.Flight;
import com.example.flightbookingsystem.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlightService {
    private final FlightRepository flightRepository;

    @Cacheable(value = "flights", key = "#id", unless = "#result == null")
    public FlightDTO getFlightById(Long id) {
        log.info("Fetching flight with id: {} from database", id);
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with id: " + id));
        return convertToDTO(flight);
    }

    @Cacheable(value = "flights", key = "'number:' + #flightNumber", unless = "#result == null")
    public FlightDTO getFlightByNumber(String flightNumber) {
        log.info("Fetching flight with number: {} from database", flightNumber);
        Flight flight = flightRepository.findByFlightNumber(flightNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with number: " + flightNumber));
        return convertToDTO(flight);
    }

    @Cacheable(value = "flightSearches",
            key = "#origin + ':' + #destination + ':' + #departureTime + ':' + #seats",
            unless = "#result == null || #result.isEmpty()")
    public List<FlightDTO> searchFlights(String origin, String destination, LocalDateTime departureTime, Integer seats) {
        log.info("Searching flights from {} to {} on {} for {} seats - from database",
                origin, destination, departureTime, seats);
        List<Flight> flights = flightRepository.searchAvailableFlights(origin, destination, departureTime, seats);
        return flights.stream().map(this::convertToDTO).toList();
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "flights", allEntries = true),
        @CacheEvict(value = "flightSearches", allEntries = true)
    })
    public FlightDTO createFlight(FlightDTO flightDTO) {
        log.info("Creating new flight: {}", flightDTO.getFlightNumber());
        Flight flight = convertToEntity(flightDTO);
        flight.setStatus(Flight.FlightStatus.SCHEDULED);
        Flight savedFlight = flightRepository.save(flight);
        log.info("Flight created successfully, cache invalidated");
        return convertToDTO(savedFlight);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "flights", key = "#id"),
        @CacheEvict(value = "flightSearches", allEntries = true)
    })
    public FlightDTO updateFlight(Long id, FlightDTO flightDTO) {
        log.info("Updating flight with id: {}", id);
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with id: " + id));

        updateFlightFromDTO(flight, flightDTO);
        Flight updatedFlight = flightRepository.save(flight);
        log.info("Flight updated successfully, cache invalidated");
        return convertToDTO(updatedFlight);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "flights", key = "#id"),
        @CacheEvict(value = "flightSearches", allEntries = true)
    })
    public void deleteFlight(Long id) {
        log.info("Deleting flight with id: {}", id);
        if (!flightRepository.existsById(id)) {
            throw new ResourceNotFoundException("Flight not found with id: " + id);
        }
        flightRepository.deleteById(id);
        log.info("Flight deleted successfully, cache invalidated");
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "flights", key = "#flightId"),
        @CacheEvict(value = "flightSearches", allEntries = true)
    })
    public boolean decreaseAvailableSeats(Long flightId, Integer seats) {
        log.info("Decreasing available seats for flight: {} by {}", flightId, seats);
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));

        if (flight.getAvailableSeats() < seats) {
            log.warn("Not enough seats available. Available: {}, Requested: {}",
                    flight.getAvailableSeats(), seats);
            return false;
        }

        flight.setAvailableSeats(flight.getAvailableSeats() - seats);
        flightRepository.save(flight);
        log.info("Seats decreased successfully, cache invalidated");
        return true;
    }

    private FlightDTO convertToDTO(Flight flight) {
        return FlightDTO.builder()
                .id(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .airline(flight.getAirline())
                .origin(flight.getOrigin())
                .destination(flight.getDestination())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .totalSeats(flight.getTotalSeats())
                .availableSeats(flight.getAvailableSeats())
                .price(flight.getPrice())
                .status(flight.getStatus().name())
                .build();
    }

    private Flight convertToEntity(FlightDTO dto) {
        return Flight.builder()
                .flightNumber(dto.getFlightNumber())
                .airline(dto.getAirline())
                .origin(dto.getOrigin())
                .destination(dto.getDestination())
                .departureTime(dto.getDepartureTime())
                .arrivalTime(dto.getArrivalTime())
                .totalSeats(dto.getTotalSeats())
                .availableSeats(dto.getAvailableSeats())
                .price(dto.getPrice())
                .build();
    }

    private void updateFlightFromDTO(Flight flight, FlightDTO dto) {
        flight.setAirline(dto.getAirline());
        flight.setOrigin(dto.getOrigin());
        flight.setDestination(dto.getDestination());
        flight.setDepartureTime(dto.getDepartureTime());
        flight.setArrivalTime(dto.getArrivalTime());
        flight.setTotalSeats(dto.getTotalSeats());
        flight.setAvailableSeats(dto.getAvailableSeats());
        flight.setPrice(dto.getPrice());
    }
}
