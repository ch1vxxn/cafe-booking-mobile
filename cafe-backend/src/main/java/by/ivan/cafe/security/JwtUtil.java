package by.ivan.cafe.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JwtUtil — JWT token generator and validator.
 *
 * What is JWT (explain during defense):
 *  "JWT is a JSON Web Token. When a user logs in, the server creates
 *   a token — a three-part string containing the username, role,
 *   and expiration date. The token is signed with a secret known
 *   only to the server, so it cannot be forged.
 *   The frontend sends this token with every request in the
 *   Authorization: Bearer <token> header, and the server knows who it is."
 *
 * Three parts of the token (separated by dots):
 *   header.payload.signature
 *  - header: which signing algorithm;
 *  - payload: the actual data (subject=phone, role, exp=expiration);
 *  - signature: signed with the secret — guarantees authenticity.
 *
 * createToken(): creates a token valid for 24 hours.
 * parseToken(): verifies the signature and extracts data.
 */
@Component
public class JwtUtil {

    /** Secret key for signing. In production stored in secrets, for coursework — in config. */
    private final SecretKey key;

    /** Token lifetime in milliseconds (24 hours). */
    private final long expirationMs;

    /**
     * Constructor: Spring injects values from application.properties
     * (properties jwt.secret and jwt.expiration-ms, defined below).
     */
    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration-ms}") long expirationMs) {
        // Convert secret string to cryptographic key for signing (HS256)
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Creates a token for a user.
     *
     * @param subject phone (unique user identifier)
     * @param role    role (GUEST/ADMIN) — stored in token to avoid DB lookup
     *                on every request
     */
    public String createToken(String subject, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(subject)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * Parses a token and returns its contents (Claims).
     * If the signature is invalid or the token expired — throws an exception,
     * and we return 401 Unauthorized to the client.
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Extract phone (subject) from token. */
    public String getPhone(String token) {
        return parseToken(token).getSubject();
    }

    /** Extract role from token. */
    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}