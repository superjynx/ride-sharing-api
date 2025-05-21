package com.example.ridesharing.filter;

import com.example.ridesharing.util.JwtUtils;
import com.example.ridesharing.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        logger.debug("Processing request for path: {}", path);

        // Skip JWT auth for public auth endpoints
        if (path.startsWith("/api/auth/")) {
            logger.debug("Skipping JWT filter for auth path");
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        logger.debug("Auth header: {}", authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String username = jwtUtils.getUsernameFromToken(token);
            logger.debug("Username from token: {}", username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                logger.debug("UserDetails loaded: {} with authorities {}", userDetails.getUsername(), userDetails.getAuthorities());

                // Validate token and set authentication
                if (jwtUtils.validateToken(token, username)) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    logger.debug("Authentication set in SecurityContext with roles: {}", auth.getAuthorities());
                    logger.debug("Request path: {}, Authorities: {}", path, auth.getAuthorities());
                    logger.debug("Is authenticated: {}", auth.isAuthenticated());
                    logger.debug("Principal: {}", auth.getPrincipal());
                    logger.debug("Credentials: {}", auth.getCredentials());
                    logger.debug("Details: {}", auth.getDetails());
                } else {
                    logger.warn("JWT token validation failed for user: {}", username);
                }
            } else {
                logger.debug("Username is null or authentication already exists in context");
            }
        } else {
            logger.debug("No valid Authorization header found");
        }

        filterChain.doFilter(request, response);
    }
}
