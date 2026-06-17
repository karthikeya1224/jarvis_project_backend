package com.expensetracker.dao.auth;

import com.expensetracker.model.User;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.sharedlibs.logger.AppLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class AuthDao {
    
    private static final Logger logger = AppLogger.getLogger(AuthDao.class);

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User registerUser(User user) {
        AppLogger.logInfo(logger, "registerUser", "Registering user with email: " + user.getEmail());
        
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            AppLogger.logError(logger, "registerUser", "Email already registered: " + user.getEmail(), new RuntimeException("Email already registered"));
            throw new RuntimeException("Email already registered");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = sanitize(userRepository.save(user));
        
        AppLogger.logInfo(logger, "registerUser", "User registered successfully with id: " + saved.getId());
        return saved;
    }

    public Map<String, Object> loginUser(String email, String password) {
        AppLogger.logInfo(logger, "loginUser", "Login attempt for email: " + email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    AppLogger.logError(logger, "loginUser", "Invalid credentials for email: " + email, new RuntimeException("Invalid email or password"));
                    return new RuntimeException("Invalid email or password");
                });
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            AppLogger.logError(logger, "loginUser", "Password mismatch for email: " + email, new RuntimeException("Invalid email or password"));
            throw new RuntimeException("Invalid email or password");
        }
        
        AppLogger.logInfo(logger, "loginUser", "User logged in successfully: " + user.getId());
        return Map.of("message", "Login successful", "userId", user.getId(), "name", user.getName(), "email", user.getEmail());
    }

    public Map<String, String> forgotPassword(String email) {
        AppLogger.logInfo(logger, "forgotPassword", "Password reset requested for email: " + email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    AppLogger.logError(logger, "forgotPassword", "No account found for email: " + email, new RuntimeException("No account found"));
                    return new RuntimeException("No account found with this email");
                });
        
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        userRepository.save(user);
        
        AppLogger.logInfo(logger, "forgotPassword", "Reset token generated for user: " + user.getId());
        return Map.of("message", "Password reset token generated", "resetToken", token);
    }

    public Map<String, String> resetPassword(String resetToken, String newPassword) {
        AppLogger.logInfo(logger, "resetPassword", "Password reset attempt with token");
        
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }
        
        User user = userRepository.findByResetToken(resetToken)
                .orElseThrow(() -> {
                    AppLogger.logError(logger, "resetPassword", "Invalid reset token", new RuntimeException("Invalid or expired reset token"));
                    return new RuntimeException("Invalid or expired reset token");
                });
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        userRepository.save(user);
        
        AppLogger.logInfo(logger, "resetPassword", "Password reset successfully for user: " + user.getId());
        return Map.of("message", "Password reset successful");
    }

    public User getUserById(String userId) {
        AppLogger.logInfo(logger, "getUserById", "Fetching user info for userId: " + userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    AppLogger.logError(logger, "getUserById", "User not found: " + userId, new RuntimeException("User not found"));
                    return new RuntimeException("User not found");
                });
        
        AppLogger.logInfo(logger, "getUserById", "User info fetched successfully");
        return sanitize(user);
    }

    public User updateUser(String userId, String name, String email) {
        AppLogger.logInfo(logger, "updateUser", "Updating user info for userId: " + userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (name != null && !name.isBlank()) user.setName(name);
        
        if (email != null && !email.isBlank()) {
            if (!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                throw new RuntimeException("Invalid email format");
            }
            userRepository.findByEmail(email).ifPresent(existing -> {
                if (!existing.getId().equals(userId)) throw new RuntimeException("Email already in use");
            });
            user.setEmail(email);
        }
        
        User updated = sanitize(userRepository.save(user));
        AppLogger.logInfo(logger, "updateUser", "User info updated successfully");
        return updated;
    }

    private User sanitize(User user) {
        user.setPassword(null);
        user.setResetToken(null);
        return user;
    }
}
