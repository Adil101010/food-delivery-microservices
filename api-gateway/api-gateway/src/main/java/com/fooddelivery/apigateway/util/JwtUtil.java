package com.fooddelivery.apigateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            System.err.println("JWT Parsing Error: " + e.getMessage());
            throw e;
        }
    }

    public String extractUserId(String token) {
        try {
            Claims claims = extractClaims(token);
            Object userId = claims.get("userId");
            return userId != null ? userId.toString() : null;
        } catch (Exception e) {
            System.err.println("Extract UserId Error: " + e.getMessage());
            return null;
        }
    }

    public String extractEmail(String token) {
        try {
            return extractClaims(token).getSubject();
        } catch (Exception e) {
            System.err.println("Extract Email Error: " + e.getMessage());
            return null;
        }
    }

    public String extractRole(String token) {
        try {
            return extractClaims(token).get("role", String.class);
        } catch (Exception e) {
            System.err.println("Extract Role Error: " + e.getMessage());
            return null;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            System.err.println("Token Validation Error: " + e.getMessage());
            return false;
        }
    }
}
