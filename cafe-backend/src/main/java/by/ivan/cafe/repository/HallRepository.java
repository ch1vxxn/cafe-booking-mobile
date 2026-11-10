package by.ivan.cafe.repository;

import by.ivan.cafe.model.Hall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Репозиторий залов. JpaRepository уже даёт:
 * findAll(), findById(), save(), deleteById() и т.д.
 * Здесь нам пока не нужны дополнительные запросы.
 */
public interface HallRepository extends JpaRepository<Hall, Long> {

    /** Найти зал по названию (для демо-данных: не дублировать при старте). */
    Optional<Hall> findByName(String name);
}