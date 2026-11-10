package by.ivan.cafe.dto;

import by.ivan.cafe.model.Booking;
import by.ivan.cafe.model.BookingStatus;
import by.ivan.cafe.model.RestaurantTable;

import java.time.LocalDateTime;

/**
 * BookingDto — «форма ответа» для брони (без циклических ссылок).
 *
 * Сущность Booking ссылается на User и RestaurantTable, а те — обратно.
 * Если отдать сущность прямо в JSON, Jackson зациклится (бесконечная
 * вложенность). Поэтому собираем плоский DTO с id и нужными полями.
 */
public record BookingDto(
        Long id,
        Long userId,
        String userName,
        String userPhone,
        Long tableId,
        int tableNumber,
        String hallName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        int guestsCount,
        String comment,
        BookingStatus status,
        String qrCode
) {
    public static BookingDto fromEntity(Booking b) {
        RestaurantTable t = b.getTable();
        return new BookingDto(
                b.getId(),
                b.getUser().getId(),
                b.getUser().getFullName(),
                b.getUser().getPhone(),
                t.getId(),
                t.getTableNumber(),
                t.getHall() != null ? t.getHall().getName() : null,
                b.getStartTime(),
                b.getEndTime(),
                b.getGuestsCount(),
                b.getComment(),
                b.getStatus(),
                b.getQrCode()
        );
    }
}