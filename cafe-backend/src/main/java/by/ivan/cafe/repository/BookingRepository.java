package by.ivan.cafe.repository;

import by.ivan.cafe.model.Booking;
import by.ivan.cafe.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Репозиторий броней — здесь живёт самое интересное.
 *
 * 1) findByTableIdAndStartTimeLessThanAndEndTimeGreaterThan(...)
 *    Spring по имени метода строит SQL:
 *    SELECT * FROM bookings
 *    WHERE table_id = ? AND start_time < ? AND end_time > ?
 *
 *    Это и есть проверка ПЕРЕСЕЧЕНИЯ интервалов. Объяснение:
 *    новая броня [newStart, newEnd] пересекается со старой [oldStart, oldEnd],
 *    если одновременно выполняется:
 *       oldStart < newEnd  (старая началась раньше, чем новая закончится)
 *       oldEnd   > newStart (старая закончится позже, чем новая начнётся)
 *
 * 2) findByUserId(...) — все брони конкретного гостя («мои брони»).
 *
 * 3) findByStatus(...) — фильтр по статусу («только новые», например).
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /** Найти пересекающиеся брони стола в интервале [start, end]. */
    List<Booking> findByTableIdAndStartTimeLessThanAndEndTimeGreaterThan(
            Long tableId, LocalDateTime start, LocalDateTime end);

    /** Все брони гостя (по id пользователя). */
    List<Booking> findByUserId(Long userId);

    /** Все брони с конкретным статусом (для админки). */
    List<Booking> findByStatus(BookingStatus status);
}