package by.ivan.cafe.dto;

import by.ivan.cafe.model.Role;

/**
 * DTO — Data Transfer Object («объект передачи данных»).
 *
 * Это НЕ таблица из базы, а «форма ответа» для фронта.
 * Мы не отдаём клиенту сущность целиком (в ней есть passwordHash,
 * лишние связи), а собираем аккуратный ответ: только то,
 * что нужно показать на экране.
 *
 * (На защите: «DTO отделяют внутреннюю модель от того, что видит клиент».)
 */
public record UserDto(Long id, String firstName, String lastName,
                      String phone, Role role, int bonusPoints) {

    public static UserDto fromEntity(
            by.ivan.cafe.model.User user) {
        return new UserDto(user.getId(), user.getFirstName(), user.getLastName(),
                user.getPhone(), user.getRole(), user.getBonusPoints());
    }
}