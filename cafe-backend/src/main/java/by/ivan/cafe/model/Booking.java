package by.ivan.cafe.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Бронь стола — центральная сущность всей системы.
 *
 * Связи:
 *  - много броней -> один гость (@ManyToOne User);
 *  - много броней -> один стол (@ManyToOne RestaurantTable).
 *
 * Почему startTime/endTime — это «сетевая логика»:
 *  когда гость бронирует стол на 19:00-21:00, сервер проверяет,
 *  не пересекается ли этот интервал с уже существующими бронями
 *  этого стола на эту дату. Эта проверка живёт на Java (в сервисе),
 *  а не на фронте — именно поэтому приложение «сетевое».
 *
 * Статусы — см. BookingStatus.java (NEW -> CONFIRMED -> COMPLETED и т.д.)
 *
 * qrCode: строка-«секрет» брони (UUID), по которой админ подтверждает
 * гостя на входе. На фронте превратим её в QR-картинку.
 */
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Гость, который сделал бронь. */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    /** Стол, который забронирован. */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "table_id")
    private RestaurantTable table;

    /** Дата и время начала брони. */
    @Column(nullable = false)
    private LocalDateTime startTime;

    /** Дата и время окончания брони. */
    @Column(nullable = false)
    private LocalDateTime endTime;

    /** Сколько гостей придёт (нужно для фильтра и понимания). */
    @Column(nullable = false)
    private int guestsCount;

    /** Комментарий гостя («день рождения», «нужен высокий стул»). */
    @Column(length = 500)
    private String comment;

    /** Статус брони (см. BookingStatus.java). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    /** Секретный код брони для QR (уникален). */
    @Column(nullable = false, unique = true, length = 36)
    private String qrCode;

    /** Когда бронь создана (для сортировки и истории). */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Booking() {}

    public Booking(User user, RestaurantTable table,
                   LocalDateTime startTime, LocalDateTime endTime,
                   int guestsCount, String comment,
                   BookingStatus status, String qrCode) {
        this.user = user;
        this.table = table;
        this.startTime = startTime;
        this.endTime = endTime;
        this.guestsCount = guestsCount;
        this.comment = comment;
        this.status = status;
        this.qrCode = qrCode;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public RestaurantTable getTable() { return table; }
    public void setTable(RestaurantTable table) { this.table = table; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public int getGuestsCount() { return guestsCount; }
    public void setGuestsCount(int guestsCount) { this.guestsCount = guestsCount; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}