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
 * TableController — просмотр столов (публичные GET).
 *
 * GET /api/tables?hallId=1        — столы зала (фильтр по залу)
 * GET /api/tables/{id}            — один стол
 * GET /api/tables/available?date=&start=&end=&guests=  — СВОБОДНЫЕ столы
 *
 * ВАЖНО: эндпоинт /available объявлен в этом контроллере, а НЕ /{id} раньше!
 * Иначе Spring воспримет слово "available" как {id} и попытается
 * превратить его в число -> ошибка "method parameter 'id': Failed to convert".
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
     * Свободные столы на интервал.
     * GET /api/tables/available?date=2026-10-22&start=19:00&end=21:00&guests=4
     *
     * Логика: берём ВСЕ столы, подходящие по вместимости, и исключаем те,
     * у которых есть пересекающаяся бронь (см. BookingService.findAvailableTables).
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
                .orElseThrow(() -> new RuntimeException("Стол не найден"));
    }
}