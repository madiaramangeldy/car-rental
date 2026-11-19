package kz.autorent.dto;

import jakarta.validation.constraints.*;
import kz.autorent.model.Car;
import kz.autorent.model.CarCategory;
import kz.autorent.model.CarStatus;

import java.math.BigDecimal;

public class CarForm {

    private Long id;

    @NotBlank(message = "Маркасын енгізіңіз")
    @Size(max = 50)
    private String brand;

    @NotBlank(message = "Моделін енгізіңіз")
    @Size(max = 50)
    private String model;

    @NotNull(message = "Шығарылған жылын енгізіңіз")
    @Min(value = 1995, message = "1995 жылдан кейінгі көлік болуы керек")
    @Max(value = 2030, message = "Жыл қате")
    private Integer manufactureYear;

    @NotBlank(message = "Мемлекеттік нөмірді енгізіңіз")
    @Pattern(regexp = "^[0-9]{3}[A-Za-z]{2,3}[0-9]{2}$", message = "Нөмір форматы: 123ABC02")
    private String plateNumber;

    @NotNull(message = "Санатты таңдаңыз")
    private CarCategory category;

    @NotNull(message = "Бағаны енгізіңіз")
    @DecimalMin(value = "1000", message = "Баға кемінде 1000 ₸")
    @DecimalMax(value = "1000000", message = "Баға тым жоғары")
    private BigDecimal pricePerDay;

    @NotNull(message = "Орын санын енгізіңіз")
    @Min(2) @Max(9)
    private Integer seats;

    @NotNull
    private CarStatus status = CarStatus.AVAILABLE;

    @Size(max = 1000)
    private String description;

    /** Ағымдағы фотоны өшіру (жаңасы жүктелмесе). */
    private boolean removePhoto;

    /** Тек көрсету үшін — формадан келген мән қолданылмайды. */
    private String currentImageFileName;

    public static CarForm from(Car car) {
        CarForm f = new CarForm();
        f.setId(car.getId());
        f.setBrand(car.getBrand());
        f.setModel(car.getModel());
        f.setManufactureYear(car.getManufactureYear());
        f.setPlateNumber(car.getPlateNumber());
        f.setCategory(car.getCategory());
        f.setPricePerDay(car.getPricePerDay());
        f.setSeats(car.getSeats());
        f.setStatus(car.getStatus());
        f.setDescription(car.getDescription());
        f.setCurrentImageFileName(car.getImageFileName());
        return f;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Integer getManufactureYear() { return manufactureYear; }
    public void setManufactureYear(Integer manufactureYear) { this.manufactureYear = manufactureYear; }
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
    public CarCategory getCategory() { return category; }
    public void setCategory(CarCategory category) { this.category = category; }
    public BigDecimal getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(BigDecimal pricePerDay) { this.pricePerDay = pricePerDay; }
    public Integer getSeats() { return seats; }
    public void setSeats(Integer seats) { this.seats = seats; }
    public CarStatus getStatus() { return status; }
    public void setStatus(CarStatus status) { this.status = status; }
    public boolean isRemovePhoto() { return removePhoto; }
    public void setRemovePhoto(boolean removePhoto) { this.removePhoto = removePhoto; }
    public String getCurrentImageFileName() { return currentImageFileName; }
    public void setCurrentImageFileName(String currentImageFileName) { this.currentImageFileName = currentImageFileName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
