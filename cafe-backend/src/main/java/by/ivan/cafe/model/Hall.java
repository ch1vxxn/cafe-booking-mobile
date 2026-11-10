package by.ivan.cafe.model;

import jakarta.persistence.*;

/**
 * Зал кафе, например «Основной зал» или «Веранда».
 *
 * Зачем отдельная сущность, если у нас одно кафе?
 *  - у кафе может быть 2-3 зала (это реально: веранда, основной зал);
 *  - столы группируются по залам — красиво показывать на схеме;
 *  - на защите это демонстрирует понимание связей «один-ко-многим».
 *
 * Связь: один зал -> много столов (Hall 1---* RestaurantTable).
 * Сами столы мы объявим в классе RestaurantTable через @ManyToOne.
 */
@Entity
@Table(name = "halls")
public class Hall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    /** Путь к картинке зала (может быть пустым — фронт покажет заглушку). */
    private String imageUrl;

    public Hall() {}

    public Hall(String name, String description, String imageUrl) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}