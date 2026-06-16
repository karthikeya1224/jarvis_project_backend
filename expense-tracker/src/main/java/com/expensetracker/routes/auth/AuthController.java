package com.expensetracker.routes.auth;

import com.expensetracker.dao.auth.AuthDao;
import com.expensetracker.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
@Tag(name = "Auth", description = "Authentication & User Management APIs")
public class AuthController {

    @Autowired
    private AuthDao authDao;

    // ─── Request DTOs ─────────────────────────────────────────────────────────

    record RegisterRequest(
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Mobile number is required") @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be 10 digits") String mobileNumber,
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
        @NotBlank(message = "Password is required") @Size(min = 6, message = "Password must be at least 6 characters") String password,
        @NotBlank(message = "Confirm password is required") String confirmPassword
    ) {}

    record LoginRequest(
        @NotBlank(message = "Email or mobile number is required") String emailOrMobile,
        @NotBlank(message = "Password is required") String password
    ) {}

    record UpdateUserRequest(
        String name,
        @Email(message = "Invalid email format") String email,
        @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be 10 digits") String mobileNumber,
        @Size(min = 6, message = "Password must be at least 6 characters") String newPassword
    ) {}

    // ─── Endpoints ────────────────────────────────────────────────────────────

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with name, mobile, email, and password")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest req) {
        User user = new User();
        user.setName(req.name());
        user.setMobileNumber(req.mobileNumber());
        user.setEmail(req.email());
        user.setPassword(req.password());
        return ResponseEntity.ok(authDao.registerUser(user, req.confirmPassword()));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email/mobile and password", description = "Authenticate user with email or mobile number and password")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authDao.loginUser(req.emailOrMobile(), req.password()));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user info by ID", description = "Retrieve user details using userId returned during registration/login")
    public ResponseEntity<?> getUserInfo(@PathVariable String userId) {
        return ResponseEntity.ok(authDao.getUserById(userId));
    }

    @PutMapping("/user/{userId}")
    @Operation(summary = "Update user profile", description = "Update user name, email, mobile number, and/or password")
    public ResponseEntity<?> updateUser(@PathVariable String userId, @Valid @RequestBody UpdateUserRequest req) {
        return ResponseEntity.ok(authDao.updateUser(userId, req.name(), req.email(), req.mobileNumber(), req.newPassword()));
    }
}
