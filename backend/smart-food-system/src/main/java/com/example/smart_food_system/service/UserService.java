package com.example.smart_food_system.service;

import com.example.smart_food_system.dto.*;
import com.example.smart_food_system.model.User;
import com.example.smart_food_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    // BCrypt encoder 
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    //  AUTH 

    public UserResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (user.getStatus() == User.Status.INACTIVE) {
            throw new RuntimeException("Account is deactivated. Contact admin.");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        return UserResponse.from(user);
    }

    public UserResponse register(RegisterRequest req) {
        // Validation
        if (req.getFirstName() == null || req.getFirstName().isBlank())
            throw new RuntimeException("First name is required");
        if (req.getLastName() == null || req.getLastName().isBlank())
            throw new RuntimeException("Last name is required");
        if (req.getEmail() == null || req.getEmail().isBlank())
            throw new RuntimeException("Email is required");
        if (req.getPassword() == null || req.getPassword().length() < 6)
            throw new RuntimeException("Password must be at least 6 characters");
        if (!req.getPassword().equals(req.getConfirmPassword()))
            throw new RuntimeException("Passwords do not match");

        String email = req.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email))
            throw new RuntimeException("Email already registered");

        // Parse role
        User.Role role = User.Role.CUSTOMER;
        if (req.getRole() != null) {
            try { role = User.Role.valueOf(req.getRole().toUpperCase()); }
            catch (IllegalArgumentException e) { role = User.Role.CUSTOMER; }
        }

        User user = User.builder()
                .firstName(req.getFirstName().trim())
                .lastName(req.getLastName().trim())
                .email(email)
                .password(passwordEncoder.encode(req.getPassword()))
                .role(role)
                .address(req.getAddress())
                .telephone(req.getTelephone())
                .status(User.Status.ACTIVE)
                .build();

        return UserResponse.from(userRepository.save(user));
    }

    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("No account found with this email"));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        // In production: send email with token
        // For demo: return the token directly
        return token;
    }

    public void resetPassword(ResetPasswordRequest req) {
        if (!req.getNewPassword().equals(req.getConfirmPassword()))
            throw new RuntimeException("Passwords do not match");
        if (req.getNewPassword().length() < 6)
            throw new RuntimeException("Password must be at least 6 characters");

        User user = userRepository.findByResetToken(req.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Reset token has expired. Please request a new one.");

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    //  CRUD 

    public List<UserResponse> getAllUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return UserResponse.from(user);
    }

    public UserResponse createUser(RegisterRequest req) {
        // Admin creating users can set any role
        return register(req);
    }

    public UserResponse updateUser(Long id, UpdateUserRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (req.getFirstName() != null && !req.getFirstName().isBlank())
            user.setFirstName(req.getFirstName().trim());
        if (req.getLastName() != null && !req.getLastName().isBlank())
            user.setLastName(req.getLastName().trim());
        if (req.getAddress() != null)
            user.setAddress(req.getAddress());
        if (req.getTelephone() != null)
            user.setTelephone(req.getTelephone());

        if (req.getRole() != null) {
            try { user.setRole(User.Role.valueOf(req.getRole().toUpperCase())); }
            catch (IllegalArgumentException e) { }
        }
        if (req.getStatus() != null) {
            try { user.setStatus(User.Status.valueOf(req.getStatus().toUpperCase())); }
            catch (IllegalArgumentException e) {}
        }

        return UserResponse.from(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        // Soft delete
        user.setStatus(User.Status.INACTIVE);
        userRepository.save(user);
    }

    public Map<String, Long> getStats() {
        return Map.of(
            "totalUsers",     userRepository.countByStatus(User.Status.ACTIVE),
            "totalAdmins",    userRepository.countByRole(User.Role.ADMIN),
            "totalStaff",     userRepository.countByRole(User.Role.STAFF),
            "totalCustomers", userRepository.countByRole(User.Role.CUSTOMER)
        );
    }
}