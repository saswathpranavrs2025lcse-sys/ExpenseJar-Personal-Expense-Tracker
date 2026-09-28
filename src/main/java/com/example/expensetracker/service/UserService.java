package com.example.expensetracker.service;

import com.example.expensetracker.dto.UserRequest;
import com.example.expensetracker.dto.UserResponse;
import com.example.expensetracker.entity.User;
import com.example.expensetracker.exception.DuplicateResourceException;
import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse createUser(UserRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("User already exists with email: " + request.getEmail());
        }

        long nextNum = 10001 + userRepository.count();
        String code = "EXP-" + nextNum;
        while (userRepository.existsByUserIdCode(code)) {
            nextNum++;
            code = "EXP-" + nextNum;
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode("password123")); // Default dev password for direct API creation
        user.setUserIdCode(code);

        User saved = userRepository.save(user);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return mapToResponse(getUserEntityById(id));
    }

    @Transactional(readOnly = true)
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public UserResponse mapToResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getUserIdCode(),
            user.getName(),
            user.getEmail(),
            user.getCreatedAt()
        );
    }
}
