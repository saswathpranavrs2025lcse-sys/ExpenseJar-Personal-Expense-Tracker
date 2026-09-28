package com.example.expensetracker.controller;

import com.example.expensetracker.dto.AuthRequest;
import com.example.expensetracker.dto.AuthResponse;
import com.example.expensetracker.dto.ChangePasswordRequest;
import com.example.expensetracker.dto.RegisterRequest;
import com.example.expensetracker.dto.UpdateProfileRequest;
import com.example.expensetracker.dto.UserResponse;
import com.example.expensetracker.security.UserPrincipal;
import com.example.expensetracker.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication & User Profile", description = "Endpoints for user registration, login, profile management, and password change")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Creates a new user account with unique sequential User ID (e.g. EXP-10001), validates input, and securely hashes password.")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates user credentials and returns a secure JWT bearer token.")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping({"/me", "/profile"})
    @Operation(summary = "Get current authenticated user profile", description = "Retrieves the currently authenticated user's profile including User ID, Name, Email, and Creation Date.")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(authService.getCurrentUserProfile(principal));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update user profile", description = "Allows updating user's display name and email address.")
    public ResponseEntity<UserResponse> updateProfile(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(authService.updateProfile(principal, request));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Allows the authenticated user to update their password securely.")
    public ResponseEntity<Map<String, String>> changePassword(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(principal, request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully."));
    }
}
