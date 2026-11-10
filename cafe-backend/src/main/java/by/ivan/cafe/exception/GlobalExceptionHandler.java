package by.ivan.cafe.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler — единый обработчик ошибок.
 *
 * Без него любое RuntimeException вернуло бы клиенту 500 с огромным
 * стектрейсом. С ним мы ловим типичные ошибки и отдаём аккуратный JSON:
 *
 *   {"error": "Стол уже занят на это время"}
 *
 * @ControllerAdvice — «перехватчик исключений для всех контроллеров».
 *
 * Обрабатываем:
 *  - RuntimeException      -> 400 Bad Request (наши бизнес-ошибки);
 *  - MethodArgumentNotValidException -> 400 со списком нарушенных полей
 *    (@Valid не прошёл: пустой телефон, пароль короче 4 символов и т.д.).
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(err ->
                errors.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Проверьте поля: " + errors));
    }
}