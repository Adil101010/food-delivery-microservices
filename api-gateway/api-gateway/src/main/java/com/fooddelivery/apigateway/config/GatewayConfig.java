package com.fooddelivery.apigateway.config;

import com.fooddelivery.apigateway.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Autowired
    private JwtAuthenticationFilter jwtFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // ========== PUBLIC ROUTES (No JWT) ==========
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .uri("http://localhost:8097"))

                // ========== PROTECTED ROUTES (JWT Required) ==========

                // User Service
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8081"))

                // Restaurant Service
                .route("restaurant-service", r -> r
                        .path("/api/restaurants/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8082"))

                // Menu Service
                .route("menu-service", r -> r
                        .path("/api/menus/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8083"))

                // Order Service
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8084"))

                // Notification Service
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8085"))

                // Delivery Service
                .route("delivery-service", r -> r
                        .path("/api/deliveries/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8086"))

                // Location Service
                .route("location-service", r -> r
                        .path("/api/locations/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8087"))

                // Assignment Service
                .route("assignment-service", r -> r
                        .path("/api/assignments/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8088"))

                // Payment Service
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8089"))

                // Review Service
                .route("review-service", r -> r
                        .path("/api/reviews/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8090"))

                // Delivery Rating Service
                .route("delivery-rating-service", r -> r
                        .path("/api/delivery-ratings/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8090"))

                // Cart Service
                .route("cart-service", r -> r
                        .path("/api/cart/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8091"))

                // Promo Service
                .route("promo-service", r -> r
                        .path("/api/coupons/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8092"))

                // Wallet Service
                .route("wallet-service", r -> r
                        .path("/api/wallet/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8093"))

                // Settlement Service
                .route("settlement-service", r -> r
                        .path("/api/settlements/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8094"))

                // Analytics Service
                .route("analytics-service", r -> r
                        .path("/api/analytics/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8095"))

                // Support Service
                .route("support-service", r -> r
                        .path("/api/support/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8096"))

                .build();
    }

    // NO CORS BEAN - Auth Service will handle CORS!
}
