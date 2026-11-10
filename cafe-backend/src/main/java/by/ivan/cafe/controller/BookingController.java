package by.ivan.cafe.controller;

import by.ivan.cafe.dto.BookingDto;
import by.ivan.cafe.dto.CreateBookingRequest;
import by.ivan.cafe.model.Booking;
import by.ivan.cafe.model.User;
import by.ivan.cafe.repository.UserRepository;
import by.ivan.cafe.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * BookingController — действия гостя с бронями.
 *
 * Все эндпоинты требуют JWT (см. SecurityConfig: /api/bookings/** -> authenticated).
 *
 * Как контроллер узнаёт, КТО делает запрос?
 *  - метод получает Authentication (Spring сам подставляет его из SecurityContext);
 *  - из токена мы знаем телефон (subject), по нему находим пользователя в БД.
 *
 * Эндпоинты:
 *  POST   /api/bookings                — создать бронь
 *  GET    /api/bookings/my             — мои брони
 *  DELETE /api/bookings/{id}           — отменить свою бронь
 */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    public BookingController(BookingService bookingService, UserRepository userRepository) {
        this.bookingService = bookingService;
        this.userRepository = userRepository;
    }

    /** Достаём пользователя из контекста безопасности по телефону. */
    private User currentUser(Authentication auth) {
        String phone = (String) auth.getPrincipal();
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    /** Создать бронь. */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateBookingRequest req,
                                    Authentication auth) {
        try {
            User user = currentUser(auth);
            Booking booking = bookingService.createBooking(
                    user, req.tableId(), req.startTime(), req.endTime(),
                    req.guestsCount(), req.comment());
            return ResponseEntity.ok(BookingDto.fromEntity(booking));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Мои брони. */
    @GetMapping("/my")
    public List<BookingDto> my(Authentication auth) {
        User user = currentUser(auth);
        return bookingService.myBookings(user).stream()
                .map(BookingDto::fromEntity)
                .toList();
    }

    /** Отменить свою бронь. */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancel(@PathVariable Long id, Authentication auth) {
        try {
            User user = currentUser(auth);
            bookingService.cancelBooking(user, id);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}