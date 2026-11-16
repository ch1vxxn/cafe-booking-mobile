package by.ivan.cafe.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig — access configuration (Spring Security).
 *
 * Here we define what can be accessed without login,
 * what requires an authenticated user, and what requires admin.
 *
 * Rules:
 *  - /api/auth/register and /api/auth/login — open to all (otherwise how
 *    would one register/log in?);
 *  - GET /api/halls, /api/tables — open (anyone can browse the cafe);
 *  - /api/bookings/** and /api/auth/me — require JWT (any authenticated user);
 *  - /api/admin/** — ADMIN role only.
 *
 * sessionManagement: stateless — we don't store server-side state,
 * we trust JWT. This is the modern REST approach.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF and basic auth — not needed for REST API.
            .csrf(AbstractHttpConfigurer::disable)
            // No HTTP sessions (we use JWT).
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Allow H2 Console (it uses iframes).
            // By default Spring sets X-Frame-Options: deny, so H2 Console
            // won't display in browser with error "Refused to display in a frame".
            // sameOrigin = allow frames from the same address (safe).
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            // Disable default login form.
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            // Access rules:
            .authorizeHttpRequests(auth -> auth
                // Open endpoints (no token):
                .requestMatchers("/api/auth/register", "/api/auth/login", "/h2-console/**").permitAll()
                // Public GET for browsing the cafe:
                .requestMatchers(HttpMethod.GET, "/api/halls/**", "/api/tables/**").permitAll()
                // ADMIN: ADMIN role only (authorization!)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // All other requests — authenticated:
                .anyRequest().authenticated()
            )
            // Insert our JWT filter before the standard
            // UsernamePasswordAuthenticationFilter — so the token
            // is processed before the access decision.
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCryptPasswordEncoder — password hashing.
     * Classic defense question:
     * "Why not store passwords in plain text?" — if the database leaks,
     * passwords can't be read. BCrypt adds a "salt" and slows down
     * brute-force. Password comparison happens via encoder.matches(...).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
