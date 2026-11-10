package by.ivan.cafe.model;

import jakarta.persistence.*;

/**
 * Стол в кафе. Называется RestaurantTable, потому что слово Table
 * зарезервировано в SQL (это классическая ловушка — объясню на защите).
 *
 * Связи:
 *  - многие столы -> один зал (@ManyToOne Hall). В базе в строке стола
 *    появится колонка hall_id, которая ссылается на id зала.
 *  - один стол -> много броней (см. Booking: @ManyToOne RestaurantTable).
 *
 * Поля:
 *  - tableNumber — номер стола в зале. Уникален В ПРЕДЕЛАХ зала
 *    (в основном зале может быть стол №1 и в веранде стол №1).
 *    Это красиво объясняет составной ключ (hall_id, tableNumber).
 *  - capacity — сколько гостей вмещает (нужно для фильтра по гостям).
 *  - location — «у окна», «у барной стойки» — фишка для UI.
 */
@Entity
@Table(name = "tables",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"hall_id", "table_number"}))
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Зал, которому принадлежит стол (многие-к-одному). */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "hall_id")
    private Hall hall;

    @Column(name = "table_number", nullable = false)
    private int tableNumber;

    @Column(nullable = false)
    private int capacity;

    /** Например «у окна», «у барной стойки», «в центре зала». */
    private String location;

    /** Картинка стола (необязательная). */
    private String imageUrl;

    public RestaurantTable() {}

    public RestaurantTable(Hall hall, int tableNumber, int capacity,
                           String location, String imageUrl) {
        this.hall = hall;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.location = location;
        this.imageUrl = imageUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Hall getHall() { return hall; }
    public void setHall(Hall hall) { this.hall = hall; }

    public int getTableNumber() { return tableNumber; }
    public void setTableNumber(int tableNumber) { this.tableNumber = tableNumber; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}