package by.ivan.cafe.repository;

import by.ivan.cafe.model.Hall;
import by.ivan.cafe.model.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Репозиторий столов.
 *
 * findByHallId(...) — Spring сгенерирует запрос:
 * SELECT * FROM tables WHERE hall_id = ?
 * Используем, когда фронт просит «все столы зала №1».
 */
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {

    /** Все столы конкретного зала (по id зала). */
    List<RestaurantTable> findByHallId(Long hallId);

    /** Столы зала с нужной вместимостью (фильтр «стол на N гостей»). */
    List<RestaurantTable> findByHallIdAndCapacityGreaterThanEqual(Long hallId, int capacity);

    /** Все столы, вмещающие N гостей или больше (для поиска свободных). */
    List<RestaurantTable> findByCapacityGreaterThanEqual(int capacity);
}