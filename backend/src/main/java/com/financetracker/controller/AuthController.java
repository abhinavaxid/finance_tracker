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

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

/**
 * REST Controller for authentication endpoints
 * Supports both JWT and Spring Session authentication
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
     * Login user - Creates both JWT and HTTP Session
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody UserLoginRequest request,
            HttpSession session) {
        log.info("Login request for email: {}", request.getEmail());
        
        User user = userService.authenticate(request.getEmail(), request.getPassword())
                .orElse(null);
        if (user == null) {
            log.warn("Authentication failed for email: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        userService.updateLastLogin(user.getId());
        
        String jwtToken = jwtTokenProvider.generateTokenFromUserId(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        
        // Create Spring Session
        session.setAttribute("USER_ID", user.getId());
        session.setAttribute("EMAIL", user.getEmail());
        session.setAttribute("FIRST_NAME", user.getFirstName());
        session.setAttribute("LAST_NAME", user.getLastName());
        
        // Set session timeout based on rememberMe if available
        if (request.isRememberMe()) {
            session.setMaxInactiveInterval(7 * 24 * 60 * 60); // 7 days
            log.info("Extended session timeout for user: {}", user.getEmail());
        } else {
            session.setMaxInactiveInterval(60 * 60); // 1 hour
        }
        
        log.info("User authenticated and session created: {}", user.getEmail());
        
        AuthResponse response = AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .token(jwtToken)
                .refreshToken(refreshToken)
                .expiresIn(86400)
                .sessionId(session.getId())
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
     * Logout user - Invalidates both JWT and HTTP Session
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpSession session) {
        log.info("Logout request");
        
        try {
            // Invalidate HTTP session
            if (session != null) {
                Long userId = (Long) session.getAttribute("USER_ID");
                String sessionId = session.getId();
                
                session.invalidate();
                log.info("Session invalidated for user: {}", userId);
            }
            
            // If JWT token provided, log expiration
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                int expirationSeconds = jwtTokenProvider.getExpirationTimeInSeconds(token);
                log.info("JWT token will expire in {} seconds", expirationSeconds);
            }
            
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

    /**
     * Get current session information
     * GET /api/auth/session
     */
    @GetMapping("/session")
    public ResponseEntity<AuthResponse> getSessionInfo(HttpSession session) {
        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Long userId = (Long) session.getAttribute("USER_ID");
        String email = (String) session.getAttribute("EMAIL");
        String firstName = (String) session.getAttribute("FIRST_NAME");
        String lastName = (String) session.getAttribute("LAST_NAME");
        
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        AuthResponse response = AuthResponse.builder()
                .userId(userId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .sessionId(session.getId())
                .build();
        
        return ResponseEntity.ok(response);
    }
}
