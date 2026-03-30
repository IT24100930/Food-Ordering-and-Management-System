package com.example.smart_food_system.config;

import com.example.smart_food_system.model.User;
import com.example.smart_food_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {
        // Only seed if no users exist
        if (userRepository.count() == 0) {
            log.info("Seeding default users...");

            userRepository.save(User.builder()
                    .firstName("System").lastName("Admin")
                    .email("admin@urbanplate.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(User.Role.ADMIN)
                    .address("123 Admin Street, Colombo")
                    .telephone("0112345678")
                    .status(User.Status.ACTIVE)
                    .build());

            userRepository.save(User.builder()
                    .firstName("John").lastName("Staff")
                    .email("staff@urbanplate.com")
                    .password(passwordEncoder.encode("Staff@123"))
                    .role(User.Role.STAFF)
                    .address("456 Staff Lane, Colombo")
                    .telephone("0119876543")
                    .status(User.Status.ACTIVE)
                    .build());

            userRepository.save(User.builder()
                    .firstName("Jane").lastName("Customer")
                    .email("customer@example.com")
                    .password(passwordEncoder.encode("Customer@123"))
                    .role(User.Role.CUSTOMER)
                    .address("789 Customer Road, Colombo")
                    .telephone("0771234567")
                    .status(User.Status.ACTIVE)
                    .build());

            log.info("Default users created:");
            log.info("   Admin    → admin@urbanplate.com    / Admin@123");
            log.info("   Staff    → staff@urbanplate.com    / Staff@123");
            log.info("   Customer → customer@example.com / Customer@123");
        } else {
            log.info("Users already exist, skipping seed.");
        }
    }
}