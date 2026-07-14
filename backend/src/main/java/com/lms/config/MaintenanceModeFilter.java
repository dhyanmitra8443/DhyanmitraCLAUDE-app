package com.lms.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.settings.service.SystemSettingsService;
import com.lms.shared.response.ApiErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Ref: SRS 16.12 - "While enabled, only administrators may access the system."
 *
 * Runs after JwtAuthenticationFilter (which is what populates the
 * SecurityContext), because the decision depends on the caller's role: an
 * ADMINISTRATOR passes through so they can still reach
 * PATCH /settings/maintenance-mode and turn it back off - locking the only
 * person who can lift maintenance mode out of the system would make it a
 * one-way door.
 *
 * Everyone else - including unauthenticated callers, who cannot be
 * administrators - gets 503 Service Unavailable, the honest status for
 * "deliberately down right now" (a 403 would wrongly suggest a permission
 * problem they could fix).
 *
 * Two exemptions: /actuator/** must keep answering so orchestration and
 * monitoring don't declare the container dead while it is intentionally in
 * maintenance, and the auth endpoints stay open so an administrator can log
 * in to lift it.
 */
@Component
@Order(MaintenanceModeFilter.ORDER)
public class MaintenanceModeFilter extends OncePerRequestFilter {

    /**
     * Spring Security's filter chain runs at order -100 (SecurityProperties
     * .DEFAULT_FILTER_ORDER), so any value above that puts this filter after
     * authentication has populated the SecurityContext.
     */
    static final int ORDER = 0;

    private final SystemSettingsService settingsService;
    private final ObjectMapper objectMapper;

    public MaintenanceModeFilter(SystemSettingsService settingsService, ObjectMapper objectMapper) {
        this.settingsService = settingsService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator")
                || path.startsWith("/api/v1/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!settingsService.isMaintenanceModeEnabled() || isAdministrator()) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getOutputStream(),
                ApiErrorResponse.of("The system is currently undergoing maintenance. Please try again later."));
    }

    private boolean isAdministrator() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRATOR"));
    }
}
