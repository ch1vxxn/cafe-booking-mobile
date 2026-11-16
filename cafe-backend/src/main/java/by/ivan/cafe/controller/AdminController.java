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
 * AdminController — admin actions (role ADMIN).
 *
 * Access: only ADMIN (see SecurityConfig: /api/admin/** -> hasRole ADMIN).
 * How do we know it's an admin? JWT filter puts the role in the context,
 * Spring Security itself blocks non-admin access (403).
 *
 * Endpoints:
 *  GET   /api/admin/bookings              — all bookings (optional status filter)
 *  PATCH /api/admin/bookings/{id}/status  — change booking status
 *
 * Status change is the "cafe workflow": called — confirmed (CONFIRMED),
 * guest arrived (COMPLETED), didn't show (NO_SHOW)...
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

    /** All bookings, optional status filter (?status=CONFIRMED). */
    @GetMapping("/bookings")
    public List<BookingDto> allBookings(
            @RequestParam(required = false) BookingStatus status,
            Authentication auth) {
        List<Booking> bookings = (status != null)
                ? bookingRepository.findByStatus(status)
                : bookingRepository.findAll();
        return bookings.stream().map(BookingDto::fromEntity).toList();
    }

    /** Change booking status (PATCH /api/admin/bookings/{id}/status {"status":"CONFIRMED"}). */
    @PatchMapping("/bookings/{id}/status")
    public ResponseEntity<?> changeStatus(@PathVariable Long id,
                                          @RequestBody Map<String, String> body,
                                          Authentication auth) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        String statusStr = body.get("status");
        if (statusStr == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Field status is required"));
        }

        BookingStatus newStatus;
        try {
            newStatus = BookingStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid status: " + statusStr));
        }

        // Loyalty effect: if booking is completed — award bonus points
        // (win-win feature: regular guests accumulate points).
        if (newStatus == BookingStatus.COMPLETED
                && booking.getStatus() != BookingStatus.COMPLETED) {
            User guest = booking.getUser();
            guest.setBonusPoints(guest.getBonusPoints() + 50); // +50 points per visit
            userRepository.save(guest);
        }

        booking.setStatus(newStatus);
        bookingRepository.save(booking);

        return ResponseEntity.ok(BookingDto.fromEntity(booking));
    }
}