package com.example.flightbookingsystem.controller;

import com.example.flightbookingsystem.dto.FlightDTO;
import com.example.flightbookingsystem.dto.FlightSearchRequest;
import com.example.flightbookingsystem.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/flights")
@RequiredArgsConstructor
@Tag(name = "Flight Management", description = "APIs for managing flights")
public class FlightController {
    private final FlightService flightService;

    @GetMapping("/{id}")
    @Operation(summary = "Get flight by ID")
    public ResponseEntity<FlightDTO> getFlightById(@PathVariable Long id) {
        return ResponseEntity.ok(flightService.getFlightById(id));
    }

    @GetMapping("/number/{flightNumber}")
    @Operation(summary = "Get flight by flight number")
    public ResponseEntity<FlightDTO> getFlightByNumber(@PathVariable String flightNumber) {
        return ResponseEntity.ok(flightService.getFlightByNumber(flightNumber));
    }

    @PostMapping("/search")
    @Operation(summary = "Search for available flights")
    public ResponseEntity<List<FlightDTO>> searchFlights(@Valid @RequestBody FlightSearchRequest request) {
        List<FlightDTO> flights = flightService.searchFlights(
                request.getOrigin(),
                request.getDestination(),
                request.getDepartureTime(),
                request.getSeats()
        );
        return ResponseEntity.ok(flights);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_admin')")
    @Operation(summary = "Create a new flight (Admin only)")
    public ResponseEntity<FlightDTO> createFlight(@Valid @RequestBody FlightDTO flightDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(flightService.createFlight(flightDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_admin')")
    @Operation(summary = "Update a flight (Admin only)")
    public ResponseEntity<FlightDTO> updateFlight(@PathVariable Long id, @Valid @RequestBody FlightDTO flightDTO) {
        return ResponseEntity.ok(flightService.updateFlight(id, flightDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_admin')")
    @Operation(summary = "Delete a flight (Admin only)")
    public ResponseEntity<Void> deleteFlight(@PathVariable Long id) {
        flightService.deleteFlight(id);
        return ResponseEntity.noContent().build();
    }
}

