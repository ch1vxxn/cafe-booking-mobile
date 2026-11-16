package by.ivan.cafe.config;

import by.ivan.cafe.model.Hall;
import by.ivan.cafe.model.RestaurantTable;
import by.ivan.cafe.model.Role;
import by.ivan.cafe.model.User;
import by.ivan.cafe.repository.HallRepository;
import by.ivan.cafe.repository.RestaurantTableRepository;
import by.ivan.cafe.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * DataSeeder — populates the database with initial data on first run.
 *
 * CommandLineRunner: Spring runs this code AFTER application startup.
 *
 * We create:
 *  - an admin (login: +375291234567 / password: admin123);
 *  - two halls: "Main Hall" and "Terrace";
 *  - tables in each hall (different capacities — provides choice).
 *
 * Why this is convenient: right after startup you have an admin and tables,
 * the frontend and tests can work without manually filling the database.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final HallRepository hallRepository;
    private final RestaurantTableRepository tableRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      HallRepository hallRepository,
                      RestaurantTableRepository tableRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.hallRepository = hallRepository;
        this.tableRepository = tableRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // ---------- Admin (only create if not exists) ----------
        if (userRepository.findByPhone("+375291234567").isEmpty()) {
            userRepository.save(new User(
                    "Иван", "Администратор",
                    "+375291234567",
                    passwordEncoder.encode("admin123"),
                    Role.ADMIN
            ));
            System.out.println("Created admin: +375291234567 / admin123");
        }

        // ---------- Halls ----------
        Hall main = hallRepository.findByName("Основной зал").orElseGet(() ->
                hallRepository.save(new Hall(
                        "Основной зал",
                        "Уютный зал с панорамными окнами и живой музыкой по пятницам",
                        null
                )));

        Hall terrace = hallRepository.findByName("Веранда").orElseGet(() ->
                hallRepository.save(new Hall(
                        "Веранда",
                        "Открытая терраса с видом на набережную. Работает в тёплое время года",
                        null
                )));

        // ---------- Tables ----------
        seedTable(main, 1, 2, "у окна");
        seedTable(main, 2, 2, "у окна");
        seedTable(main, 3, 4, "в центре зала");
        seedTable(main, 4, 4, "в центре зала");
        seedTable(main, 5, 6, "у барной стойки");
        seedTable(main, 6, 8, "VIP-зона");

        seedTable(terrace, 1, 4, "у входа на веранду");
        seedTable(terrace, 2, 2, "у входа на веранду");
        seedTable(terrace, 3, 6, "с видом на набережную");
        seedTable(terrace, 4, 8, "с видом на набережную");

        System.out.println("Demo data ready: 2 halls, "
                + tableRepository.count() + " tables, "
                + userRepository.count() + " users.");
    }

    /** Create a table if it doesn't exist yet (by hall + number). */
    private void seedTable(Hall hall, int number, int capacity, String location) {
        boolean exists = tableRepository.findAll().stream()
                .anyMatch(t -> t.getHall().getId().equals(hall.getId())
                        && t.getTableNumber() == number);
        if (!exists) {
            tableRepository.save(new RestaurantTable(hall, number, capacity, location, null));
        }
    }
}