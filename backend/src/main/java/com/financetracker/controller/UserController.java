package com.financetracker.controller;

import com.financetracker.dto.request.UserUpdateRequest;
import com.financetracker.dto.response.UserResponse;
import com.financetracker.model.User;
import com.financetracker.repository.UserRepository;
import com.financetracker.service.UserService;
import com.financetracker.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST Controller for user profile management
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * Get current user profile
     * GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        log.info("Fetching current user profile");
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return ResponseEntity.ok(user);
    }

    /**
     * Get user by ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        log.info("Fetching user: {}", id);
        
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Update user profile
     * PUT /api/users/me
     */
    @PutMapping("/me")
    public ResponseEntity<User> updateProfile(
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("Updating user profile");
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return ResponseEntity.ok(user);
    }

    /**
     * Deactivate user account
     * DELETE /api/users/me
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deactivateAccount() {
        log.info("Deactivating user account");
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        userService.deactivateUser(user.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Check if email exists
     * GET /api/users/check-email
     */
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email) {
        log.info("Checking if email exists: {}", email);
        boolean exists = userService.emailExists(email);
        return ResponseEntity.ok(exists);
    }
}
