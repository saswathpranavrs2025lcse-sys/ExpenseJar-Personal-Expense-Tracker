package com.example.expensetracker.service;

import com.example.expensetracker.dto.AuthRequest;
import com.example.expensetracker.dto.AuthResponse;
import com.example.expensetracker.dto.ChangePasswordRequest;
import com.example.expensetracker.dto.RegisterRequest;
import com.example.expensetracker.dto.UpdateProfileRequest;
import com.example.expensetracker.dto.UserResponse;
import com.example.expensetracker.entity.User;
import com.example.expensetracker.exception.DuplicateResourceException;
import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.security.JwtService;
import com.example.expensetracker.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService,
        AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("An account with this email already exists.");
        }

        String userIdCode = generateUniqueUserIdCode();

        User user = new User();
        user.setName(request.getFullName().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserIdCode(userIdCode);

        User savedUser = userRepository.save(user);

        UserPrincipal principal = UserPrincipal.create(savedUser);
        String token = jwtService.generateToken(principal, false);

        return new AuthResponse(token, mapToUserResponse(savedUser), "Account created successfully.");
    }

    public AuthResponse login(AuthRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
            );

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            String token = jwtService.generateToken(principal, request.isRememberMe());

            return new AuthResponse(token, mapToUserResponse(user), "Login successful");
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid email or password.");
        }
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + principal.getId()));
        return mapToUserResponse(user);
    }

    public UserResponse updateProfile(UserPrincipal principal, UpdateProfileRequest request) {
        User user = userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + principal.getId()));

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailAndIdNot(normalizedEmail, user.getId())) {
            throw new DuplicateResourceException("An account with this email already exists.");
        }

        user.setName(request.getName().trim());
        user.setEmail(normalizedEmail);

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    public void changePassword(UserPrincipal principal, ChangePasswordRequest request) {
        User user = userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + principal.getId()));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("New passwords do not match.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private synchronized String generateUniqueUserIdCode() {
        long base = 10001 + userRepository.count();
        String code = "EXP-" + base;
        while (userRepository.existsByUserIdCode(code)) {
            base++;
            code = "EXP-" + base;
        }
        return code;
    }

    public UserResponse mapToUserResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getUserIdCode(),
            user.getName(),
            user.getEmail(),
            user.getCreatedAt()
        );
    }
}
