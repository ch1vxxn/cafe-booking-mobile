package by.ivan.cafe.service;

import by.ivan.cafe.model.Booking;
import by.ivan.cafe.model.BookingStatus;
import by.ivan.cafe.model.RestaurantTable;
import by.ivan.cafe.model.User;
import by.ivan.cafe.repository.BookingRepository;
import by.ivan.cafe.repository.RestaurantTableRepository;
import by.ivan.cafe.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * BookingService — booking business logic.
 *
 * This is the CORE of the application: the main validation logic —
 * checking whether a table is already booked for the selected time.
 *
 * createBooking logic:
 *  1) verify the table exists;
 *  2) check that time is not in the past and end > start;
 *  3) FIND overlapping bookings for this table (key point!);
 *     overlap = existing booking starts before new end AND
 *               existing booking ends after new start;
 *  4) if overlap exists — throw exception ("table already booked");
 *  5) if free — create booking with status NEW and QR code (UUID).
 *
 * QR code: a unique string used by the admin to look up the booking at the entrance.
 */
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RestaurantTableRepository tableRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
                          RestaurantTableRepository tableRepository,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.tableRepository = tableRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a booking.
     *
     * @param user        guest who is booking (from JWT)
     * @param tableId     table id
     * @param startTime   start time (e.g. 2026-10-22T19:00)
     * @param endTime     end time   (e.g. 2026-10-22T21:00)
     * @param guestsCount number of guests
     * @param comment     optional comment
     */
    @Transactional
    public Booking createBooking(User user, Long tableId,
                                 LocalDateTime startTime, LocalDateTime endTime,
                                 int guestsCount, String comment) {

        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found"));

        // Basic time checks — prevent booking in the past
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot book in the past");
        }
        if (!endTime.isAfter(startTime)) {
            throw new RuntimeException("End time must be after start time");
        }
        if (guestsCount > table.getCapacity()) {
            throw new RuntimeException("Table max capacity is " + table.getCapacity() + " guests");
        }

        // ============ MAIN: overlap check ============
        // Find bookings for this table that overlap with our interval.
        // The repository builds the SQL:
        //   WHERE table_id = ? AND start_time < end AND end_time > start
        List<Booking> overlapping = bookingRepository
                .findByTableIdAndStartTimeLessThanAndEndTimeGreaterThan(
                        tableId, endTime, startTime);

        if (!overlapping.isEmpty()) {
            throw new RuntimeException("Table already booked for this time slot");
        }

        // Free — create booking with status NEW and QR code.
        Booking booking = new Booking(
                user, table, startTime, endTime, guestsCount, comment,
                BookingStatus.NEW,
                UUID.randomUUID().toString()
        );
        return bookingRepository.save(booking);
    }

    /**
     * Find AVAILABLE tables for interval [from, to] for N guests.
     *
     * Logic (same as when creating a booking, but inverted):
     *  1) get all tables with the required capacity;
     *  2) for each, check: is there an overlapping booking?
     *  3) if none — the table is free, add to the result.
     *
     * Used by the frontend in real time: when the guest selects
     * date/time, they immediately see which tables are free.
     */
    public List<RestaurantTable> findAvailableTables(LocalDateTime from,
                                                     LocalDateTime to,
                                                     int guests) {
        if (!to.isAfter(from)) {
            throw new RuntimeException("End time must be after start time");
        }
        List<RestaurantTable> candidates = tableRepository
                .findByCapacityGreaterThanEqual(guests);

        return candidates.stream()
                .filter(table -> bookingRepository
                        .findByTableIdAndStartTimeLessThanAndEndTimeGreaterThan(
                                table.getId(), to, from)
                        .isEmpty())
                .toList();
    }

    /** List of bookings for a specific guest ("my bookings"). */
    public List<Booking> myBookings(User user) {
        return bookingRepository.findByUserId(user.getId());
    }

    /** Cancel a booking (owner only). */
    @Transactional
    public void cancelBooking(User user, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("This is not your booking");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED
                || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking has already ended");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }
}