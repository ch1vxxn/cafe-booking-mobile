package by.ivan.cafe.controller;

import by.ivan.cafe.dto.AuthResponse;
import by.ivan.cafe.dto.UserDto;
import by.ivan.cafe.model.User;
import by.ivan.cafe.security.JwtUtil;
import by.ivan.cafe.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AuthController — HTTP-вход в систему: регистрация и логин.
 *
 * @RestController — «контроллер», который принимает HTTP-запросы
 * и возвращает JSON. Spring сам превращает Java-объект в JSON.
 *
 * Эндпоинты:
 *  POST /api/auth/register  — создать аккаунт
 *  POST /api/auth/login     — войти, получить токен
 *
 * Под капотом: сервис AuthService + генерация JWT (JwtUtil).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Тело запроса — простые JSON-объекты (record). @Valid включает проверки
     * (@NotBlank — поле не пустое, @Size — минимальная длина пароля).
     * Так мы отсекаем «мусор» ещё до бизнес-логики.
     */
    public record RegisterRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank String phone,
            @NotBlank @Size(min = 4) String password
    ) {}

    public record LoginRequest(
            @NotBlank String phone,
            @NotBlank String password
    ) {}

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        try {
            User user = authService.register(
                    req.firstName(), req.lastName(), req.phone(), req.password());
            String token = jwtUtil.createToken(user.getPhone(), user.getRole().name());
            return ResponseEntity.ok(new AuthResponse(token, UserDto.fromEntity(user)));
        } catch (RuntimeException e) {
            // «Телефон уже зарегистрирован»
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            User user = authService.login(req.phone(), req.password());
            String token = jwtUtil.createToken(user.getPhone(), user.getRole().name());
            return ResponseEntity.ok(new AuthResponse(token, UserDto.fromEntity(user)));
        } catch (RuntimeException e) {
            // «Неверный телефон или пароль»
            return ResponseEntity.status(401).body(error(e.getMessage()));
        }
    }

    /** Маленький помощник: оборачивает сообщение об ошибке в {"error": "..."} */
    private Map<String, String> error(String message) {
        Map<String, String> map = new HashMap<>();
        map.put("error", message);
        return map;
    }
}