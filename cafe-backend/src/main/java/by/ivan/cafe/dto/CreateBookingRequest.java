package by.ivan.cafe.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Тело запроса на создание брони (JSON от фронта).
 *
 * @NotNull — поле обязательно; @Future — время в будущем;
 * @Min — минимум гостей 1.
 *
 * Spring сам провалидирует эти поля до вызова сервиса
 * (благодаря @Valid в контроллере) и вернёт 400 при ошибке.
 */
public record CreateBookingRequest(
        @NotNull Long tableId,
        @NotNull @Future LocalDateTime startTime,
        @NotNull @Future LocalDateTime endTime,
        @Min(1) int guestsCount,
        String comment
) {}