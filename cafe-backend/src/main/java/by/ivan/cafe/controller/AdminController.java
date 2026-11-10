package by.ivan.cafe.controller;

import by.ivan.cafe.dto.BookingDto;
import by.ivan.cafe.model.Booking;
import by.ivan.cafe.model.BookingStatus;
import by.ivan.cafe.model.User;
import by.ivan.cafe.repository.BookingRepository;
import by.ivan.cafe.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AdminController — действия администратора (роль ADMIN).
 *
 * Доступ: только ADMIN (см. SecurityConfig: /api/admin/** -> hasRole ADMIN).
 * Как мы узнаём, что это админ? JWT-фильтр кладёт роль в контекст,
 * Spring Security сам запрещает доступ не-админам (403).
 *
 * Эндпоинты:
 *  GET   /api/admin/bookings              — все брони (с фильтром по статусу опц.)
 *  PATCH /api/admin/bookings/{id}/status  — сменить статус брони
 *
 * Смена статуса — и есть «кафеский путь»: позвонил, подтвердил (CONFIRMED),
 * гость пришёл (COMPLETED), не пришёл (NO_SHOW)...
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public AdminController(BookingRepository bookingRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    /** Все брони, опционально фильтр по статусу (?status=CONFIRMED). */
    @GetMapping("/bookings")
    public List<BookingDto> allBookings(
            @RequestParam(required = false) BookingStatus status,
            Authentication auth) {
        List<Booking> bookings = (status != null)
                ? bookingRepository.findByStatus(status)
                : bookingRepository.findAll();
        return bookings.stream().map(BookingDto::fromEntity).toList();
    }

    /** Сменить статус брони (POST /api/admin/bookings/{id}/status {"status":"CONFIRMED"}). */
    @PatchMapping("/bookings/{id}/status")
    public ResponseEntity<?> changeStatus(@PathVariable Long id,
                                          @RequestBody Map<String, String> body,
                                          Authentication auth) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Бронь не найдена"));

        String statusStr = body.get("status");
        if (statusStr == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Поле status обязательно"));
        }

        BookingStatus newStatus;
        try {
            newStatus = BookingStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Неверный статус: " + statusStr));
        }

        // Эффект на лояльность: если бронь завершена — начисляем гостю бонусы
        // (это наша win-win фишка: постоянные гости накапливают баллы).
        if (newStatus == BookingStatus.COMPLETED
                && booking.getStatus() != BookingStatus.COMPLETED) {
            User guest = booking.getUser();
            guest.setBonusPoints(guest.getBonusPoints() + 50); // +50 баллов за визит
            userRepository.save(guest);
        }

        booking.setStatus(newStatus);
        bookingRepository.save(booking);

        return ResponseEntity.ok(BookingDto.fromEntity(booking));
    }
}