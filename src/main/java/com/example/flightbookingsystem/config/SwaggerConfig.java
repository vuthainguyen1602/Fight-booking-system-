package com.example.flightbookingsystem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI flightBookingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Flight Booking System API")
                        .description("REST API for Flight Booking Management System")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Flight Booking Team")
                                .email("support@flightbooking.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
