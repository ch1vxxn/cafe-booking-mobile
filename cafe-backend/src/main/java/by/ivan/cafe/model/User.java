package by.ivan.cafe.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Пользователь системы: гость (GUEST) или администратор (ADMIN).
 *
 * @Entity — говорит Hibernate: «этот класс будет таблицей в базе данных».
 * Каждое поле класса станет колонкой, каждый объект — строкой таблицы.
 *
 * Как мы это объясним на защите:
 *  - пароль храним НЕ в открытом виде, а в виде хэша (BCrypt),
 *    поэтому в классе поле называется passwordHash;
 *  - телефон уникален — это «логин» гостя, по нему админ находит клиента;
 *  - bonusPoints — наша фишка лояльности: гость копит баллы за визиты,
 *    админ видит постоянных клиентов.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    /** Телефон — уникальный «логин» гостя. Один телефон = один аккаунт. */
    @Column(nullable = false, unique = true)
    private String phone;

    /** Хэш пароля (не сам пароль!) — считается алгоритмом BCrypt. */
    @Column(nullable = false)
    private String passwordHash;

    /** Роль: GUEST или ADMIN (см. Role.java) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /** Бонусные баллы за посещения (лояльность). По умолчанию 0. */
    @Column(nullable = false)
    private int bonusPoints = 0;

    // ---------- Конструкторы ----------

    /** Конструктор без аргументов ОБЯЗАТЕЛЕН для JPA (Hibernate создаёт объекты сам). */
    public User() {}

    /** Конструктор для создания пользователя в коде (регистрация). */
    public User(String firstName, String lastName, String phone,
                String passwordHash, Role role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // ---------- Геттеры и сеттеры ----------
    // JPA и Spring обращаются к полям через них. (Сокращения: можно
    // сгенерировать в IDE, но я выписал явно, чтобы ты видел все поля.)

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public int getBonusPoints() { return bonusPoints; }
    public void setBonusPoints(int bonusPoints) { this.bonusPoints = bonusPoints; }

    /** Удобное «полное имя» для отображения в списках. */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}