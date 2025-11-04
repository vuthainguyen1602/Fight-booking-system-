package com.example.flightbookingsystem.service;

import com.example.flightbookingsystem.dto.UserDTO;
import com.example.flightbookingsystem.exception.ResourceNotFoundException;
import com.example.flightbookingsystem.model.User;
import com.example.flightbookingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Cacheable(value = "users", key = "#email", unless = "#result == null")
    public UserDTO getUserByEmail(String email) {
        log.info("Fetching user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return convertToDTO(user);
    }

    @Cacheable(value = "users", key = "#id", unless = "#result == null")
    public UserDTO getUserById(Long id) {
        log.info("Fetching user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return convertToDTO(user);
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public UserDTO createOrUpdateUserFromAuth0(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String email = jwt.getClaimAsString("email");
        String auth0Id = jwt.getSubject();

        User user = userRepository.findByEmail(email)
                .orElse(User.builder()
                        .email(email)
                        .auth0Id(auth0Id)
                        .firstName(jwt.getClaimAsString("given_name"))
                        .lastName(jwt.getClaimAsString("family_name"))
                        .role(determineRole(jwt))
                        .active(true)
                        .build());

        user.setAuth0Id(auth0Id);
        user = userRepository.save(user);

        log.info("User synchronized from Auth0: {}", email);
        return convertToDTO(user);
    }

    private User.Role determineRole(Jwt jwt) {
        var permissions = jwt.getClaimAsStringList("permissions");
        if (permissions != null && permissions.contains("admin")) {
            return User.Role.ADMIN;
        }
        return User.Role.USER;
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .active(user.getActive())
                .build();
    }
}

