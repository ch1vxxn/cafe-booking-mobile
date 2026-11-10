package by.ivan.cafe.service;

import by.ivan.cafe.model.Role;
import by.ivan.cafe.model.User;
import by.ivan.cafe.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * AuthService — бизнес-логика регистрации и входа.
 *
 * register(...):  создаёт нового гостя.
 *   - если телефон уже занят — кидаем RuntimeException (контроллер
 *     превратит её в HTTP 400 «Телефон уже зарегистрирован»);
 *   - хэшируем пароль (никогда не храним открытым!);
 *   - роль: если пользователей ещё нет — даём ADMIN (первый всегда админ),
 *     иначе — GUEST. Это упрощение для курсовой, чтобы у тебя сразу
 *     был админ для теста. («В проде роль выдаёт суперадмин».)
 *
 * login(...): ищем по телефону, сверяем пароль через encoder.matches.
 *             Если не совпал — «Неверный телефон или пароль».
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Регистрация нового пользователя (гостя). */
    public User register(String firstName, String lastName,
                         String phone, String rawPassword) {
        if (userRepository.existsByPhone(phone)) {
            throw new RuntimeException("Телефон уже зарегистрирован");
        }

        Role role = userRepository.count() == 0 ? Role.ADMIN : Role.GUEST;

        User user = new User(
                firstName, lastName, phone,
                passwordEncoder.encode(rawPassword), // хэшируем!
                role
        );
        return userRepository.save(user);
    }

    /** Проверка логина: телефон + пароль. Возвращает пользователя при успехе. */
    public User login(String phone, String rawPassword) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Неверный телефон или пароль"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("Неверный телефон или пароль");
        }
        return user;
    }
}