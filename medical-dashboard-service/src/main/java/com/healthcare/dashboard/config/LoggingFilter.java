package com.healthcare.dashboard.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.logging.Logger;

@Component
@Slf4j
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {

            if (auth instanceof OAuth2AuthenticationToken oauth2Auth) {
                Object principal = oauth2Auth.getPrincipal();
                if (principal instanceof OidcUser oidcUser) {
                    log.info("🔐 [OIDC Auth] Claims: {}", oidcUser.getClaims());
                    log.info("👤 Authorities: {}", oidcUser.getAuthorities());
                } else {
                    log.info("Authenticated principal is not an OidcUser: {}", principal.getClass());
                }
            }
        } else {
            log.info("No authentication found in security context");
        }

        filterChain.doFilter(request, response);
    }
}

