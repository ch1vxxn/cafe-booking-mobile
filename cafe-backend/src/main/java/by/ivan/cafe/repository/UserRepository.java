package by.ivan.cafe.repository;

import by.ivan.cafe.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Репозиторий — «мост» между Java и базой данных.
 *
 * Spring Data JPA сам реализует все методы JpaRepository (save, findById,
 * findAll, delete...), нам не нужно писать SQL вручную.
 *
 * Специальные методы:
 *  - findByPhone(...) — Spring по ИМЕНИ метода сам поймёт, какой запрос
 *    составить: SELECT * FROM users WHERE phone = ?.
 *    (Работает по правилу: find + By + ИмяПоля.)
 *
 * Optional<User> — «коробка», которая либо содержит пользователя, либо пуста.
 * Так мы аккуратно обрабатываем случай «такого телефона нет» без null-ошибок.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /** Найти пользователя по телефону (телефон уникален). */
    Optional<User> findByPhone(String phone);

    /** Существует ли пользователь с таким телефоном (для проверки при регистрации). */
    boolean existsByPhone(String phone);
}