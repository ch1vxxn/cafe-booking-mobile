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
 * BookingService — бизнес-логика бронирования.
 *
 * Это СЕРДЦЕ курсовой: здесь живёт главная сетевая логика —
 * проверка, не занят ли стол на выбранное время.
 *
 * Логика createBooking:
 *  1) проверяем, что стол существует;
 *  2) проверяем, что время не в прошлом и end > start;
 *  3) ИЩЕМ пересекающиеся брони этого стола (ключевой момент!);
 *     пересечение = старая началась раньше конца новой И
 *                    старая закончится позже начала новой;
 *  4) если пересечение есть — кидаем исключение («стол занят»);
 *  5) если свободно — создаём бронь со статусом NEW и QR-кодом (UUID).
 *
 * QR-код: уникальная строка, по которой админ находит бронь на входе.
 * Здорово, что всё это можно объяснить преподавателю простыми словами.
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
     * Создать бронь.
     *
     * @param user        гость, который бронирует (берём из JWT)
     * @param tableId     id стола
     * @param startTime   начало (например 2026-10-22T19:00)
     * @param endTime     конец   (например 2026-10-22T21:00)
     * @param guestsCount сколько гостей
     * @param comment     комментарий (может быть null)
     */
    @Transactional
    public Booking createBooking(User user, Long tableId,
                                 LocalDateTime startTime, LocalDateTime endTime,
                                 int guestsCount, String comment) {

        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Стол не найден"));

        // Базовые проверки времени — чтобы нельзя было бронировать в прошлом
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Нельзя бронировать в прошлом");
        }
        if (!endTime.isAfter(startTime)) {
            throw new RuntimeException("Время окончания должно быть позже начала");
        }
        if (guestsCount > table.getCapacity()) {
            throw new RuntimeException("Стол вмещает максимум " + table.getCapacity() + " гостей");
        }

        // ============ ГЛАВНОЕ: проверка пересечений ============
        // Ищем брони этого стола, которые пересекаются с нашим интервалом.
        // Метод репозитория сам строит SQL:
        //   WHERE table_id = ? AND start_time < end AND end_time > start
        List<Booking> overlapping = bookingRepository
                .findByTableIdAndStartTimeLessThanAndEndTimeGreaterThan(
                        tableId, endTime, startTime);

        if (!overlapping.isEmpty()) {
            throw new RuntimeException("Стол уже занят на это время");
        }

        // Свободно — создаём бронь со статусом NEW и QR-кодом.
        Booking booking = new Booking(
                user, table, startTime, endTime, guestsCount, comment,
                BookingStatus.NEW,
                UUID.randomUUID().toString()
        );
        return bookingRepository.save(booking);
    }

    /**
     * Найти СВОБОДНЫЕ столы на интервал [from, to] для N гостей.
     *
     * Логика (та же, что при создании брони, но наоборот):
     *  1) берём все столы с нужной вместимостью;
     *  2) для каждого проверяем: есть ли пересекающаяся бронь?
     *  3) если нет — стол свободен, добавляем в результат.
     *
     * Используется фронтом в реальном времени: когда гость выбирает
     * дату/время, он сразу видит, какие столы свободны.
     */
    public List<RestaurantTable> findAvailableTables(LocalDateTime from,
                                                     LocalDateTime to,
                                                     int guests) {
        if (!to.isAfter(from)) {
            throw new RuntimeException("Время окончания должно быть позже начала");
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

    /** Список броней конкретного гостя («мои брони»). */
    public List<Booking> myBookings(User user) {
        return bookingRepository.findByUserId(user.getId());
    }

    /** Отменить бронь (может только владелец). */
    @Transactional
    public void cancelBooking(User user, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронь не найдена"));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Это чужая бронь");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED
                || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Бронь уже завершена");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }
}