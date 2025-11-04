package com.example.flightbookingsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FlightBookingSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlightBookingSystemApplication.class, args);
    }
}
