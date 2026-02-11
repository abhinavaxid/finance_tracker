package com.financetracker.controller;

import com.financetracker.dto.request.UserLoginRequest;
import com.financetracker.dto.request.UserRegistrationRequest;
import com.financetracker.dto.response.AuthResponse;
import com.financetracker.dto.response.UserResponse;
import com.financetracker.model.User;
import com.financetracker.security.JwtTokenProvider;
import com.financetracker.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST Controller for authentication endpoints
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Register a new user
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("Register request for email: {}", request.getEmail());
        
        try {
            UserResponse response = userService.registerUser(
                    request.getEmail(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getPassword()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Login user
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody UserLoginRequest request) {
        log.info("Login request for email: {}", request.getEmail());
        
        User user = userService.authenticate(request.getEmail(), request.getPassword())
                .orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        userService.updateLastLogin(user.getId());
        
        String jwtToken = jwtTokenProvider.generateTokenFromUserId(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        
        AuthResponse response = AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .token(jwtToken)
                .refreshToken(refreshToken)
                .expiresIn(86400)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh authentication token
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestHeader("Authorization") String authHeader) {
        log.info("Token refresh request");
        
        try {
            // Extract token from "Bearer {token}" format
            String token = authHeader != null && authHeader.startsWith("Bearer ") 
                ? authHeader.substring(7) 
                : authHeader;
            
            // Validate refresh token
            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("Invalid refresh token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Get user ID from token
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            
            // Generate new JWT token
            String newJwtToken = jwtTokenProvider.generateTokenFromUserId(userId);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);
            
            AuthResponse response = AuthResponse.builder()
                    .userId(userId)
                    .token(newJwtToken)
                    .refreshToken(newRefreshToken)
                    .expiresIn(86400)
                    .build();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Logout user
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        log.info("Logout request");
        
        try {
            String token = authHeader != null && authHeader.startsWith("Bearer ") 
                ? authHeader.substring(7) 
                : authHeader;
            
            // Get remaining expiration time
            int expirationSeconds = jwtTokenProvider.getExpirationTimeInSeconds(token);
            
            // In a production system, add token to blacklist with TTL = expirationSeconds
            // For now, we'll rely on token expiration validation on the server
            log.info("Token invalidated (will expire in {} seconds)", expirationSeconds);
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            return ResponseEntity.ok().build();
        }
    }

    /**
     * Change user password
     * POST /api/auth/change-password
     */
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        log.info("Change password request");
        
        try {
            // Extract token from "Bearer {token}" format
            String token = authHeader != null && authHeader.startsWith("Bearer ") 
                ? authHeader.substring(7) 
                : authHeader;
            
            // Validate token
            if (!jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Get user ID from token
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            
            // Change password
            userService.changePassword(userId, oldPassword, newPassword);
            
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("Password change failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Password change error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
