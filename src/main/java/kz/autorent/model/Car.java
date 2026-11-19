package kz.autorent.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cars")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String brand;

    @Column(nullable = false, length = 50)
    private String model;

    /** "year" сөзі H2-де резервтелген, сондықтан баған атауы басқаша. */
    @Column(name = "manufacture_year", nullable = false)
    private int manufactureYear;

    @Column(nullable = false, unique = true, length = 15)
    private String plateNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CarCategory category;

    /** Тәуліктік баға, теңгемен. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerDay;

    private int seats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CarStatus status = CarStatus.AVAILABLE;

    @Column(length = 1000)
    private String description;

    /** Жүктелген фото файлының атауы (UUID.jpg). null болса — санат иллюстрациясы көрсетіледі. */
    @Column(length = 100)
    private String imageFileName;

    /** Оптимистік бұғаттау: екі әкімші бір уақытта өзгертсе, біреуі қате алады. */
    @Version
    private Long version;

    public String getTitle() {
        return brand + " " + model;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getManufactureYear() { return manufactureYear; }
    public void setManufactureYear(int manufactureYear) { this.manufactureYear = manufactureYear; }
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
    public CarCategory getCategory() { return category; }
    public void setCategory(CarCategory category) { this.category = category; }
    public BigDecimal getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(BigDecimal pricePerDay) { this.pricePerDay = pricePerDay; }
    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }
    public CarStatus getStatus() { return status; }
    public void setStatus(CarStatus status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageFileName() { return imageFileName; }
    public void setImageFileName(String imageFileName) { this.imageFileName = imageFileName; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
