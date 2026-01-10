package com.shoply.backend.security.jwt;


import com.shoply.backend.security.service.UserDetailsImpl;
import com.shoply.backend.security.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.ZoneId;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {
    
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthTokenFilter(JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        logger.debug("AuthTokenFilter called for URI: {}", request.getRequestURI());
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // Validate Token Timestamp against Last Logout
                UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetails;
                if (userDetailsImpl.getLastLogoutDate() != null) {
                    java.util.Date issuedAt = jwtUtils.getIssuedAtFromJwtToken(jwt);
                    java.time.LocalDateTime issuedTime = issuedAt.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                    
                    if (issuedTime.isBefore(userDetailsImpl.getLastLogoutDate())) {
                        logger.warn("Rejected invalidated token for user: {}. Issued: {}, Logout: {}", 
                                username, issuedTime, userDetailsImpl.getLastLogoutDate());
                        filterChain.doFilter(request, response);
                        return;
                    }
                }
                
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails,
                                null,
                                userDetails.getAuthorities());
                logger.debug("Roles from JWT: {}", userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String jwt = jwtUtils.getJwtFromHeader(request); // Check Header first
        if (jwt != null) {
            return jwt;
        }
        return jwtUtils.getJwtFromCookies(request); // Fallback to Cookie
    }
}