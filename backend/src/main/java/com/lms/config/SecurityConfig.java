package com.lms.config;

import com.lms.config.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Ref: SRS 2.9, 3.1, 3.15 - stateless JWT authentication with RBAC.
 * @EnableMethodSecurity turns on @PreAuthorize("hasRole('ADMINISTRATOR')")
 * style checks on controller methods, which is how each module enforces
 * the role restrictions documented as `x-roles` in openapi.yaml.
 *
 * Login itself (Ref: SRS 3.6) is deliberately NOT wired through Spring
 * Security's AuthenticationManager/UserDetailsService here - for a
 * stateless JWT API, the auth module's login service can just load the
 * user by email and call passwordEncoder().matches(...) directly, which
 * avoids depending on a UserDetailsService bean that doesn't exist until
 * that module is implemented.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Endpoints with `security: []` in openapi.yaml - the single source of
     * truth for "public" here, shared by both the filter (skips JWT parsing)
     * and the authorization rules below (permits the request regardless of
     * authentication), so the two can never drift apart.
     */
    @Bean
    public RequestMatcher publicEndpoints() {
        return new OrRequestMatcher(
                new AntPathRequestMatcher("/api/v1/auth/register", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/api/v1/auth/login", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/api/v1/auth/refresh-token", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/api/v1/auth/forgot-password", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/api/v1/auth/reset-password", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/api/v1/auth/instructors/invitations/*", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/v1/auth/instructors/accept-invitation", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/api/v1/courses/**", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/v1/categories/**", HttpMethod.GET.name()),
                // Ref: SRS 7.11 - the designated preview lesson (and its
                // resources) must be viewable without authentication;
                // LessonService.assertAccess/LessonResourceService already
                // enforce the real per-lesson rule (admin/assigned instructor,
                // published+preview, or an active subscription) - these three
                // GETs were missing from the allowlist, so an anonymous
                // request never reached that logic at all.
                new AntPathRequestMatcher("/api/v1/lessons/*", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/v1/lessons/*/resources", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/v1/resources/*/download", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/v1/payments/razorpay/webhook", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/api/v1/certificates/verify/*", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/uploads/**", HttpMethod.GET.name()),
                // Ref: SRS 8.8, 8.15, 17.24 - protected by their own signed,
                // time-limited token (SignedUrlService), not a JWT; the
                // upload token itself is only ever handed out to an already
                // JWT-authenticated ADMIN/INSTRUCTOR via POST /lesson-resources/upload-url.
                new AntPathRequestMatcher("/api/v1/lesson-resources/upload/*", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/api/v1/lesson-resources/download/*", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/v1/certificate-files/download/*", HttpMethod.GET.name()),
                // Ref: SRS 15.15 - same signed-token pattern: the link is only
                // ever handed to the JWT-authenticated caller who requested the
                // export, via GET /reports/exports/{exportJobId}.
                new AntPathRequestMatcher("/api/v1/report-files/download/*", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/actuator/health"),
                new AntPathRequestMatcher("/v3/api-docs/**"),
                new AntPathRequestMatcher("/swagger-ui/**"),
                new AntPathRequestMatcher("/swagger-ui.html")
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RequestMatcher publicEndpoints
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Ref: SRS 17.6 - stateless JWT API, no cookie-based session to protect
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(publicEndpoints).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Ref: SRS 3.3, 3.9 - passwords stored only as BCrypt hashes
    }
}
