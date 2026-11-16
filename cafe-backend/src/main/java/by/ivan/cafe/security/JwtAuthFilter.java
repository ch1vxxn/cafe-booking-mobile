package by.ivan.cafe.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JwtAuthFilter — "security guard at the entrance".
 *
 * Every HTTP request passes through this filter. It:
 *  1) reads the Authorization: Bearer <token> header;
 *  2) validates the token (signature + expiration) via JwtUtil;
 *  3) if valid — stores "who it is" in the SecurityContext.
 *
 * SecurityContext is the "current user of the request".
 * Any @RestController can then ask "who is making this request?",
 * and Spring injects data from the context.
 *
 * If there is no token or it's invalid — the filter simply passes the request
 * without authentication (SecurityConfig then decides where access is allowed
 * without a token).
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // If header is missing or not in "Bearer ..." format — continue
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7); // strip "Bearer " (7 chars)

        try {
            Claims claims = jwtUtil.parseToken(token);
            String phone = claims.getSubject();
            String role = claims.get("role", String.class);

            if (phone != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Create the "authorized user" object and put it in the context.
                // ROLE_ prefix is a Spring Security requirement (role prefix).
                var authentication = new UsernamePasswordAuthenticationToken(
                        phone, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // Token invalid/expired — just don't authorize.
            // (Spring Security returns 401 where authentication is required.)
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}