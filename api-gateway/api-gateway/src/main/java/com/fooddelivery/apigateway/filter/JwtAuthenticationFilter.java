package com.fooddelivery.apigateway.filter;

import com.fooddelivery.apigateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            try {
                ServerHttpRequest request = exchange.getRequest();

                // Check Authorization header
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
                }

                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return onError(exchange, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
                }

                String token = authHeader.substring(7);

                // Validate token
                if (!jwtUtil.isTokenValid(token)) {
                    return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
                }

                // Extract claims
                String userId = jwtUtil.extractUserId(token);
                String email = jwtUtil.extractEmail(token);
                String role = jwtUtil.extractRole(token);

                // Log for debugging
                System.out.println("JWT Filter - UserId: " + userId + ", Email: " + email + ", Role: " + role);

                // Add headers
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-User-Id", userId != null ? userId : "")
                        .header("X-User-Email", email != null ? email : "")
                        .header("X-User-Role", role != null ? role : "")
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                System.err.println("JWT Filter Exception: " + e.getMessage());
                e.printStackTrace();
                return onError(exchange, "Token validation failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        System.err.println("JWT Filter Error: " + message + " | Status: " + status);
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
    }
}
