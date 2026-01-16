package com.fooddelivery.userservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class RequestHeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Extract headers added by Gateway
        String userId = request.getHeader("X-User-Id");
        String userEmail = request.getHeader("X-User-Email");
        String userRole = request.getHeader("X-User-Role");

        // If headers present, set authentication
        if (userId != null && userEmail != null && userRole != null) {
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + userRole);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userEmail,
                            null,
                            Collections.singletonList(authority)
                    );

            // Add userId to authentication details
            authentication.setDetails(userId);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
