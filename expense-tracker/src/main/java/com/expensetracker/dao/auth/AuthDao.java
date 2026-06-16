package com.expensetracker.dao.auth;

import com.expensetracker.model.User;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.sharedlibs.logger.AppLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthDao {

    private static final AppLogger log = AppLogger.getLogger(AuthDao.class);

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Map<String, Object> registerUser(User user, String confirmPassword) {
        log.info("Attempting to register user with email: {}", user.getEmail());
        
        if (!user.getPassword().equals(confirmPassword)) {
            log.warn("Registration failed: Passwords do not match");
            throw new RuntimeException("Passwords do not match");
        }
        
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            log.warn("Registration failed: Email already registered - {}", user.getEmail());
            throw new RuntimeException("Email already registered");
        }
        
        if (userRepository.findByMobileNumber(user.getMobileNumber()).isPresent()) {
            log.warn("Registration failed: Mobile number already registered - {}", user.getMobileNumber());
            throw new RuntimeException("Mobile number already registered");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {} with ID: {}", savedUser.getEmail(), savedUser.getId());
        
        return Map.of(
            "message", "User registered successfully",
            "userId", savedUser.getId(),
            "name", savedUser.getName(),
            "email", savedUser.getEmail(),
            "mobileNumber", savedUser.getMobileNumber()
        );
    }

    public Map<String, Object> loginUser(String emailOrMobile, String password) {
        log.info("Login attempt for: {}", emailOrMobile);
        
        User user = userRepository.findByEmailOrMobileNumber(emailOrMobile, emailOrMobile)
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found - {}", emailOrMobile);
                    return new RuntimeException("Invalid credentials");
                });
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Login failed: Invalid password for - {}", emailOrMobile);
            throw new RuntimeException("Invalid credentials");
        }
        
        log.info("User logged in successfully: {}", emailOrMobile);
        return Map.of(
            "message", "Login successful",
            "userId", user.getId(),
            "name", user.getName(),
            "email", user.getEmail(),
            "mobileNumber", user.getMobileNumber()
        );
    }

    public User getUserById(String userId) {
        log.info("Fetching user by ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new RuntimeException("User not found");
                });
        return sanitize(user);
    }

    public User updateUser(String userId, String name, String email, String mobileNumber, String newPassword) {
        log.info("Updating user with ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Update failed: User not found with ID: {}", userId);
                    return new RuntimeException("User not found");
                });
        
        if (name != null && !name.isBlank()) {
            user.setName(name);
        }
        
        if (email != null && !email.isBlank()) {
            if (!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                log.warn("Update failed: Invalid email format - {}", email);
                throw new RuntimeException("Invalid email format");
            }
            userRepository.findByEmail(email).ifPresent(existing -> {
                if (!existing.getId().equals(userId)) {
                    log.warn("Update failed: Email already in use - {}", email);
                    throw new RuntimeException("Email already in use");
                }
            });
            user.setEmail(email);
        }
        
        if (mobileNumber != null && !mobileNumber.isBlank()) {
            if (!mobileNumber.matches("^[0-9]{10}$")) {
                log.warn("Update failed: Invalid mobile number format - {}", mobileNumber);
                throw new RuntimeException("Mobile number must be 10 digits");
            }
            userRepository.findByMobileNumber(mobileNumber).ifPresent(existing -> {
                if (!existing.getId().equals(userId)) {
                    log.warn("Update failed: Mobile number already in use - {}", mobileNumber);
                    throw new RuntimeException("Mobile number already in use");
                }
            });
            user.setMobileNumber(mobileNumber);
        }
        
        if (newPassword != null && !newPassword.isBlank()) {
            if (newPassword.length() < 6) {
                log.warn("Update failed: Password too short");
                throw new RuntimeException("Password must be at least 6 characters");
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            log.info("Password updated for user: {}", userId);
        }
        
        User updatedUser = sanitize(userRepository.save(user));
        log.info("User updated successfully: {}", userId);
        return updatedUser;
    }

    private User sanitize(User user) {
        user.setPassword(null);
        return user;
    }
}
