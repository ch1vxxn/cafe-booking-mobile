package by.ivan.cafe.controller;

import by.ivan.cafe.model.Hall;
import by.ivan.cafe.repository.HallRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * HallController — viewing cafe halls (public GET).
 *
 * GET /api/halls        — list all halls
 * GET /api/halls/{id}   — one hall by id
 *
 * Open to all (see SecurityConfig): anyone can browse the cafe
 * without logging in — it's the "showcase" for guests.
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
                .orElseThrow(() -> new RuntimeException("Hall not found"));
    }
}