package by.ivan.cafe.dto;

/**
 * Ответ на логин/регистрацию: фронт получает JWT-токен
 * и данные пользователя.
 *
 * Токен фронт кладёт в localStorage и шлёт с каждым запросом.
 * Данные пользователя нужны, чтобы сразу показать «Привет, Иван!».
 */
public record AuthResponse(String token, UserDto user) {}