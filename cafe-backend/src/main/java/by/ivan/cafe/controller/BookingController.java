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
 * BookingController — guest actions with bookings.
 *
 * All endpoints require JWT (see SecurityConfig: /api/bookings/** -> authenticated).
 *
 * How does the controller know WHO is making the request?
 *  - the method receives Authentication (Spring injects it from SecurityContext);
 *  - from the token we know the phone (subject), then find the user in DB.
 *
 * Endpoints:
 *  POST   /api/bookings                — create a booking
 *  GET    /api/bookings/my             — my bookings
 *  DELETE /api/bookings/{id}           — cancel own booking
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

    /** Look up user from security context by phone. */
    private User currentUser(Authentication auth) {
        String phone = (String) auth.getPrincipal();
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /** Create a booking. */
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

    /** My bookings. */
    @GetMapping("/my")
    public List<BookingDto> my(Authentication auth) {
        User user = currentUser(auth);
        return bookingService.myBookings(user).stream()
                .map(BookingDto::fromEntity)
                .toList();
    }

    /** Cancel own booking. */
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