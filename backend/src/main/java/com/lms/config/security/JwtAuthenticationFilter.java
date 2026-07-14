package com.lms.config.security;

import com.lms.auth.entity.SessionStatus;
import com.lms.auth.repository.UserSessionRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Ref: SRS 2.9, 3.6, 3.7, 3.15 - validates the Bearer JWT on every request
 * and populates the SecurityContext with a UserPrincipal so downstream
 * @PreAuthorize role checks and controller-level @AuthenticationPrincipal
 * injection both work.
 *
 * Runs on every request, including endpoints permitAll allows through
 * unauthenticated (SecurityConfig.publicEndpoints) - some of those (e.g.
 * GET /courses) still need to recognize an authenticated admin for
 * optional extra behavior, so "public" only ever means "authorization
 * doesn't require it," never "skip parsing a token if one is present."
 *
 * Also enforces SRS 3.7 single-active-session: an access token whose
 * session was superseded by a later login (or logged out) is rejected even
 * if its signature/expiry are still valid, since AuthService.login()
 * deactivates the previous session's row rather than trying to revoke
 * already-issued access tokens directly.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserSessionRepository userSessionRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserSessionRepository userSessionRepository) {
        this.jwtService = jwtService;
        this.userSessionRepository = userSessionRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        extractBearerToken(request)
                .flatMap(jwtService::parseAndValidate)
                .filter(jwtService::isAccessToken)
                .filter(this::hasActiveSession)
                .ifPresent(claims -> authenticate(request, claims));
        filterChain.doFilter(request, response);
    }

    private boolean hasActiveSession(Claims claims) {
        return userSessionRepository.existsByIdAndStatus(jwtService.extractSessionId(claims), SessionStatus.ACTIVE);
    }

    private Optional<String> extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return Optional.of(header.substring(BEARER_PREFIX.length()));
        }
        return Optional.empty();
    }

    private void authenticate(HttpServletRequest request, Claims claims) {
        UserPrincipal principal = new UserPrincipal(
                jwtService.extractUserId(claims),
                jwtService.extractEmail(claims),
                jwtService.extractRole(claims),
                jwtService.extractSessionId(claims)
        );
        var authToken = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
