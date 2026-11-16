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
 * AuthController — HTTP entry point for registration and login.
 *
 * @RestController is a "controller" that receives HTTP requests
 * and returns JSON. Spring automatically converts Java objects to JSON.
 *
 * Endpoints:
 *  POST /api/auth/register  — create an account
 *  POST /api/auth/login     — log in, get a token
 *
 * Under the hood: AuthService + JWT generation (JwtUtil).
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
     * Request body — simple JSON objects (record). @Valid enables validation
     * (@NotBlank — field not empty, @Size — minimum password length).
     * This filters out junk before reaching business logic.
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
            // "Phone already registered"
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
            // "Invalid phone or password"
            return ResponseEntity.status(401).body(error(e.getMessage()));
        }
    }

    /** Little helper: wraps an error message into {"error": "..."} */
    private Map<String, String> error(String message) {
        Map<String, String> map = new HashMap<>();
        map.put("error", message);
        return map;
    }
}