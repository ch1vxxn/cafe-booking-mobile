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
 * JwtAuthFilter — «охранник на входе».
 *
 * Каждый HTTP-запрос проходит через этот фильтр. Он:
 *  1) берёт заголовок  Authorization: Bearer <токен>;
 *  2) проверяет токен (подпись + срок) через JwtUtil;
 *  3) если валиден — кладёт «кто это» в SecurityContext.
 *
 * SecurityContext — это «текущий пользователь запроса».
 * Дальше любой @RestController может спросить: «кто сейчас делает запрос?»,
 * и Spring подставит данные из контекста.
 *
 * Если токена нет или он битый — фильтр просто пропускает запрос дальше
 * без авторизации (а уже SecurityConfig решит, куда пускать без токена).
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

        // Если заголовка нет или он не в формате "Bearer ..." — идём дальше
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7); // убираем "Bearer " (7 символов)

        try {
            Claims claims = jwtUtil.parseToken(token);
            String phone = claims.getSubject();
            String role = claims.get("role", String.class);

            if (phone != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Создаём объект «авторизованный пользователь» и кладём в контекст.
                // ROLE_ префикс — требование Spring Security (префикс ролей).
                var authentication = new UsernamePasswordAuthenticationToken(
                        phone, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // Токен невалиден/просрочен — просто не авторизуем. 
            // (Spring Security сам вернёт 401 там, где вход обязателен.)
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}