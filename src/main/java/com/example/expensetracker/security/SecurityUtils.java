package com.example.expensetracker.security;

import com.example.expensetracker.repository.UserRepository;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private static UserRepository userRepository;

    public SecurityUtils(UserRepository userRepository) {
        SecurityUtils.userRepository = userRepository;
    }

    public static Optional<UserPrincipal> getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal) {
            return Optional.of((UserPrincipal) principal);
        }
        if (principal instanceof UserDetails && userRepository != null) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByEmail(username).map(UserPrincipal::create);
        }
        if (principal instanceof String && userRepository != null) {
            return userRepository.findByEmail((String) principal).map(UserPrincipal::create);
        }

        return Optional.empty();
    }

    public static Optional<Long> getCurrentUserId() {
        return getCurrentUserPrincipal().map(UserPrincipal::getId);
    }

    public static Optional<String> getCurrentUserEmail() {
        return getCurrentUserPrincipal().map(UserPrincipal::getEmail);
    }
}
