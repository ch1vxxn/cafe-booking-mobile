package by.ivan.cafe.service;

import by.ivan.cafe.model.Role;
import by.ivan.cafe.model.User;
import by.ivan.cafe.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * AuthService — registration and login business logic.
 *
 * register(...):  creates a new guest.
 *   - if phone is already taken — throw RuntimeException (controller
 *     turns it into HTTP 400 "Phone already registered");
 *   - hash the password (never store in plain text!);
 *   - role: if no users exist yet — assign ADMIN (first user is always admin),
 *     otherwise — GUEST. This simplifies the coursework so you immediately
 *     have an admin for testing. ("In production, a super-admin assigns roles.")
 *
 * login(...): look up by phone, verify password via encoder.matches.
 *             If mismatch — "Invalid phone or password".
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Register a new user (guest). */
    public User register(String firstName, String lastName,
                         String phone, String rawPassword) {
        if (userRepository.existsByPhone(phone)) {
            throw new RuntimeException("Phone already registered");
        }

        Role role = userRepository.count() == 0 ? Role.ADMIN : Role.GUEST;

        User user = new User(
                firstName, lastName, phone,
                passwordEncoder.encode(rawPassword), // hash!
                role
        );
        return userRepository.save(user);
    }

    /** Login check: phone + password. Returns the user on success. */
    public User login(String phone, String rawPassword) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Invalid phone or password"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("Invalid phone or password");
        }
        return user;
    }
}