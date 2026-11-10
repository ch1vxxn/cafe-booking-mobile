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
 * JwtUtil — генератор и проверщик JWT-токенов.
 *
 * Что такое JWT (читай вслух на защите):
 *  «JWT — это JSON Web Token. Когда пользователь логинится, сервер
 *   создаёт токен — строку из трёх частей, в которой зашифрованы
 *   имя пользователя, роль и срок действия. Токен подписан секретом,
 *   который знает только сервер, поэтому его нельзя подделать.
 *   Дальше фронт шлёт этот токен с каждым запросом в заголовке
 *   Authorization: Bearer <токен>, и сервер понимает, кто это».
 *
 * Три части токена (разделены точками):
 *   header.payload.signature
 *  - header: какой алгоритм подписи;
 *  - payload: сами данные (subject=телефон, role, exp=срок);
 *  - signature: подпись секретом — гарантия подлинности.
 *
 * createToken(): создаёт токен на 24 часа.
 * parseToken(): проверяет подпись и достаёт данные.
 */
@Component
public class JwtUtil {

    /** Секрет для подписи. В проде хранят в секретах, для курсовой — в конфиге. */
    private final SecretKey key;

    /** Время жизни токена в миллисекундах (24 часа). */
    private final long expirationMs;

    /**
     * Конструктор: Spring сам подставит значения из application.properties
     * (свойства jwt.secret и jwt.expiration-ms, зададим их следом).
     */
    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration-ms}") long expirationMs) {
        // Из строки-секрета делаем криптографический ключ для подписи (HS256)
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Создаёт токен для пользователя.
     *
     * @param subject телефон (уникальный идентификатор пользователя)
     * @param role    роль (GUEST/ADMIN) — кладём в токен, чтобы не ходить в БД
     *                при каждом запросе
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
     * Разбирает токен и возвращает его содержимое (Claims).
     * Если подпись неверна или срок истёк — кинет исключение,
     * и мы вернём клиенту 401 Unauthorized.
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Извлекает телефон (subject) из токена. */
    public String getPhone(String token) {
        return parseToken(token).getSubject();
    }

    /** Извлекает роль из токена. */
    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}