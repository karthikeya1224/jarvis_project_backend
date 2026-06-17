package com.expensetracker.routes.auth;

import com.expensetracker.dao.auth.AuthDao;
import com.expensetracker.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

    record RegisterRequest(
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
        @NotBlank(message = "Password is required") @Size(min = 6, message = "Password must be at least 6 characters") String password
    ) {}

    record LoginRequest(
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
        @NotBlank(message = "Password is required") String password
    ) {}

    record UpdateUserRequest(String name, @Email(message = "Invalid email format") String email) {}

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest req) {
        User user = new User();
        user.setName(req.name());
        user.setEmail(req.email());
        user.setPassword(req.password());
        return ResponseEntity.ok(authDao.registerUser(user));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authDao.loginUser(req.email(), req.password()));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user info by ID")
    public ResponseEntity<?> getUserInfo(@PathVariable String userId) {
        return ResponseEntity.ok(authDao.getUserById(userId));
    }

    @PutMapping("/user/{userId}")
    @Operation(summary = "Update user name and/or email")
    public ResponseEntity<?> updateUser(@PathVariable String userId, @Valid @RequestBody UpdateUserRequest req) {
        return ResponseEntity.ok(authDao.updateUser(userId, req.name(), req.email()));
    }
}
