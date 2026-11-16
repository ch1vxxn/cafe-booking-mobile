package by.ivan.cafe.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler — unified error handler.
 *
 * Without it, any RuntimeException would return 500 with a huge
 * stack trace. With it, we catch typical errors and return a clean JSON:
 *
 *   {"error": "Table already booked for this time slot"}
 *
 * @ControllerAdvice — "exception interceptor for all controllers".
 *
 * Handles:
 *  - RuntimeException      -> 400 Bad Request (our business errors);
 *  - MethodArgumentNotValidException -> 400 with list of invalid fields
 *    (@Valid failed: empty phone, password shorter than 4 chars, etc.).
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
                .body(Map.of("error", "Check fields: " + errors));
    }
}