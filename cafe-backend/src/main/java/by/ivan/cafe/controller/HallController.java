package by.ivan.cafe.controller;

import by.ivan.cafe.model.Hall;
import by.ivan.cafe.repository.HallRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * HallController — просмотр залов кафе (публичные GET).
 *
 * GET /api/halls        — список всех залов
 * GET /api/halls/{id}   — один зал по id
 *
 * Доступ открыт всем (см. SecurityConfig): посмотреть кафе можно
 * без логина — это «витрина» для гостя.
 */
@RestController
@RequestMapping("/api/halls")
public class HallController {

    private final HallRepository hallRepository;

    public HallController(HallRepository hallRepository) {
        this.hallRepository = hallRepository;
    }

    @GetMapping
    public List<Hall> all() {
        return hallRepository.findAll();
    }

    @GetMapping("/{id}")
    public Hall byId(@PathVariable Long id) {
        return hallRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Зал не найден"));
    }
}