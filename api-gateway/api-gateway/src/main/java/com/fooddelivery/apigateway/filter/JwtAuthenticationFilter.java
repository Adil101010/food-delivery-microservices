package com.fooddelivery.apigateway.filter;

import com.fooddelivery.apigateway.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    // Public endpoints that don't require JWT authentication
    private static final List<String> OPEN_ENDPOINTS = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/api/restaurants",
            "/api/menu",
            "/api/reviews/restaurant",
            "/api/delivery-ratings",
            "/actuator/health"
    );

    // Protected endpoints that explicitly require JWT authentication
    private static final List<String> PROTECTED_ENDPOINTS = Arrays.asList(
            "/api/users/profile",
            "/api/favorites",
            "/api/orders",
            "/api/cart",
            "/api/notifications/user",
            "/api/reviews/add"
    );

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            try {
                ServerHttpRequest request = exchange.getRequest();
                String path = request.getURI().getPath();
                String method = request.getMethod().toString();

                logger.info("JWT Filter processing: {} {}", method, path);

                // Check if this is a public endpoint
                if (isOpenEndpoint(path)) {
                    logger.debug("Public endpoint detected, skipping JWT validation: {}", path);
                    return chain.filter(exchange);
                }

                // Validate Authorization header presence
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    logger.warn("Missing Authorization header for protected endpoint: {}", path);
                    return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
                }

                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

                // Validate Authorization header format
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    logger.warn("Invalid Authorization header format for endpoint: {}", path);
                    return onError(exchange, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
                }

                String token = authHeader.substring(7);

                logger.debug("Token received (first 20 chars): {}...",
                        token.substring(0, Math.min(20, token.length())));

                // Validate JWT token
                if (!jwtUtil.isTokenValid(token)) {
                    logger.error("Invalid or expired JWT token for endpoint: {}", path);
                    return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
                }

                // Extract user information from token
                String userId = jwtUtil.extractUserId(token);
                String email = jwtUtil.extractEmail(token);
                String role = jwtUtil.extractRole(token);

                logger.info("JWT validation successful - UserId: {}, Email: {}, Role: {}",
                        userId, email, role);

                // Add user information to request headers for downstream services
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-User-Id", userId != null ? userId : "")
                        .header("X-User-Email", email != null ? email : "")
                        .header("X-User-Role", role != null ? role : "")
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                logger.error("JWT Filter exception: {}", e.getMessage(), e);
                return onError(exchange, "Token validation failed: " + e.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        };
    }

    /**
     * Check if the given path matches any public endpoint
     */
    private boolean isOpenEndpoint(String path) {
        return OPEN_ENDPOINTS.stream()
                .anyMatch(path::startsWith);
    }

    /**
     * Check if the given path matches any protected endpoint
     */
    private boolean isProtectedEndpoint(String path) {
        return PROTECTED_ENDPOINTS.stream()
                .anyMatch(path::startsWith);
    }

    /**
     * Handle authentication errors
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        logger.error("Authentication error: {} | Status: {}", message, status);
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");

        String errorBody = String.format(
                "{\"error\":\"%s\",\"status\":%d,\"timestamp\":\"%s\"}",
                message,
                status.value(),
                java.time.Instant.now().toString()
        );

        byte[] bytes = errorBody.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        org.springframework.core.io.buffer.DataBuffer buffer =
                exchange.getResponse().bufferFactory().wrap(bytes);

        return exchange.getResponse().writeWith(reactor.core.publisher.Mono.just(buffer));
    }

    public static class Config {
        // Configuration properties can be added here if needed
    }
}
