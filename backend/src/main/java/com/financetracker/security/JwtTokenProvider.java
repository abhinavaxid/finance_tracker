package com.financetracker.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

/**
 * JWT Token Provider for generating and validating JWT tokens
 * Handles all JWT token operations including creation and validation
 */
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwtSecret:mySecretKeyForJWTTokenGenerationAndValidationPurposeMustBeAtLeast32Characters}")
    private String jwtSecret;

    @Value("${app.jwtExpiration:86400000}") // 24 hours in milliseconds
    private long jwtExpiration;

    @Value("${app.jwtRefreshExpiration:604800000}") // 7 days in milliseconds
    private long refreshExpiration;

    /**
     * Generate JWT token from authentication
     */
    public String generateToken(Authentication authentication) {
        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();
        return generateTokenFromUserId(userPrincipal.getId());
    }

    /**
     * Generate JWT token from user ID
     */
    public String generateTokenFromUserId(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return buildToken(userId, now, expiryDate);
    }

    /**
     * Generate refresh token
     */
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);

        return buildToken(userId, now, expiryDate);
    }

    /**
     * Build JWT token
     */
    private String buildToken(Long userId, Date issuedAt, Date expiration) {
        SecretKey key = getSigningKey();

        return Jwts.builder()
                .setSubject(Long.toString(userId))
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Get user ID from JWT token
     */
    public Long getUserIdFromToken(String token) {
        try {
            SecretKey key = getSigningKey();
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return Long.parseLong(claims.getSubject());
        } catch (JwtException | NumberFormatException e) {
            log.error("Failed to get user ID from token", e);
            throw new JwtException("Invalid token", e);
        }
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String authToken) {
        try {
            SecretKey key = getSigningKey();
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Get the remaining time (in seconds) before token expiration
     */
    public int getExpirationTimeInSeconds(String token) {
        try {
            SecretKey key = getSigningKey();
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return (int) ((claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000);
        } catch (JwtException e) {
            log.error("Failed to get expiration time", e);
            return 0;
        }
    }

    /**
     * Get signing key from secret
     */
    private SecretKey getSigningKey() {
        byte[] decodedKey;
        
        // Check if the secret is already Base64 encoded
        try {
            decodedKey = Base64.getDecoder().decode(jwtSecret);
        } catch (IllegalArgumentException e) {
            // If decoding fails, use the raw string
            decodedKey = jwtSecret.getBytes(StandardCharsets.UTF_8);
        }
        
        // Ensure key is at least 32 bytes (256 bits) for HS512
        if (decodedKey.length < 32) {
            // Pad with zeros if necessary
            byte[] paddedKey = new byte[32];
            System.arraycopy(decodedKey, 0, paddedKey, 0, decodedKey.length);
            decodedKey = paddedKey;
        }
        
        return Keys.hmacShaKeyFor(decodedKey);
    }
}
