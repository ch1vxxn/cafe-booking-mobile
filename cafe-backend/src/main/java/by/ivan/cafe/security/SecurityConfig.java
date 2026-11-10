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
 * SecurityConfig — настройка доступа (Spring Security).
 *
 * Здесь мы говорим системе, что можно делать без логина,
 * а что — только авторизованным гостям или только админу.
 *
 * Правила:
 *  - /api/auth/register и /api/auth/login — открыты всем (как иначе
 *    зарегистрироваться/войти?);
 *  - GET /api/halls, /api/tables — открыты (посмотреть кафе может любой);
 *  - /api/bookings/** и /api/auth/me — требуют JWT (любой авторизованный);
 *  - /api/admin/** — только с ролью ADMIN.
 *
 * sessionManagement: создаём БЕЗ сессий (stateless) — мы не храним
 * состояние на сервере, а доверяем JWT. Это современный подход для REST.
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
            // Отключаем CSRF и базовую авторизацию — для REST API не нужны.
            .csrf(AbstractHttpConfigurer::disable)
            // Не создаём HTTP-сессии (мы на JWT).
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Разрешаем показ H2 Console (она работает через iframe/фреймы).
            // По умолчанию Spring ставит X-Frame-Options: deny и H2 Console
            // не отображается в браузере с ошибкой "Refused to display in a frame".
            // sameOrigin = показывать фреймы только с того же адреса (безопасно).
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            // Отключаем форму логина по умолчанию.
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            // Правила доступа:
            .authorizeHttpRequests(auth -> auth
                // Открытые эндпоинты (без токена):
                .requestMatchers("/api/auth/register", "/api/auth/login", "/h2-console/**").permitAll()
                // Публичные GET для просмотра кафе:
                .requestMatchers(HttpMethod.GET, "/api/halls/**", "/api/tables/**").permitAll()
                // АДМИНКА: только для роли ADMIN (авторизация!)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Остальные запросы — только авторизованные:
                .anyRequest().authenticated()
            )
            // Встраиваем наш JWT-фильтр перед стандартным фильтром
            // UsernamePasswordAuthenticationFilter — чтобы токен
            // обрабатывался до принятия решения о доступе.
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCryptPasswordEncoder — хэширование паролей.
     * Классический вопрос на защите:
     * «Почему не хранить пароль открыто?» — если база утечёт,
     * пароли не прочитают. BCrypt сам добавляет «соль» и замедляет
     * перебор. Сравнение пароля происходит через encoder.matches(...).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}