package by.ivan.cafe.controller;

import by.ivan.cafe.model.RestaurantTable;
import by.ivan.cafe.repository.RestaurantTableRepository;
import by.ivan.cafe.service.BookingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * TableController — viewing tables (public GET).
 *
 * GET /api/tables?hallId=1        — tables in a hall (filter by hall)
 * GET /api/tables/{id}            — one table
 * GET /api/tables/available?date=&start=&end=&guests=  — AVAILABLE tables
 *
 * IMPORTANT: the /available endpoint is declared FIRST in this controller,
 * NOT /{id}! Otherwise Spring treats "available" as {id} and tries
 * to convert it to a number -> error "method parameter 'id': Failed to convert".
 */
@RestController
@RequestMapping("/api/tables")
public class TableController {

    private final RestaurantTableRepository tableRepository;
    private final BookingService bookingService;

    public TableController(RestaurantTableRepository tableRepository,
                           BookingService bookingService) {
        this.tableRepository = tableRepository;
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<RestaurantTable> all(@RequestParam(required = false) Long hallId,
                                     @RequestParam(required = false) Integer guests) {
        if (hallId != null && guests != null) {
            return tableRepository.findByHallIdAndCapacityGreaterThanEqual(hallId, guests);
        }
        if (hallId != null) {
            return tableRepository.findByHallId(hallId);
        }
        return tableRepository.findAll();
    }

    /**
     * Available tables for a time interval.
     * GET /api/tables/available?date=2026-10-22&start=19:00&end=21:00&guests=4
     *
     * Logic: take ALL tables matching capacity, exclude those
     * that have an overlapping booking (see BookingService.findAvailableTables).
     */
    @GetMapping("/available")
    public List<RestaurantTable> available(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(defaultValue = "1") int guests) {

        LocalDateTime from = LocalDateTime.parse(date + "T" + start);
        LocalDateTime to = LocalDateTime.parse(date + "T" + end);

        return bookingService.findAvailableTables(from, to, guests);
    }

    @GetMapping("/{id}")
    public RestaurantTable byId(@PathVariable Long id) {
        return tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table not found"));
    }
}