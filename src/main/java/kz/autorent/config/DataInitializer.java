package kz.autorent.config;

import kz.autorent.model.Car;
import kz.autorent.model.CarCategory;
import kz.autorent.model.Role;
import kz.autorent.model.User;
import kz.autorent.repository.CarRepository;
import kz.autorent.repository.UserRepository;
import kz.autorent.service.ImageStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

/**
 * Бос дерекқорды демо-деректермен толтырады (3 пайдаланушы + 8 көлік).
 * Сонымен қатар src/main/resources/seed-photos/ ішіндегі фотоларды көліктерге тіркейді
 * (файл атауы = мемлекеттік нөмір, мысалы 404DDD02.jpg).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageStorageService imageStorage;

    @Value("${app.seed.admin-password}")
    private String adminPassword;
    @Value("${app.seed.manager-password}")
    private String managerPassword;
    @Value("${app.seed.client-password}")
    private String clientPassword;

    public DataInitializer(UserRepository userRepository, CarRepository carRepository,
                           PasswordEncoder passwordEncoder, ImageStorageService imageStorage) {
        this.userRepository = userRepository;
        this.carRepository = carRepository;
        this.passwordEncoder = passwordEncoder;
        this.imageStorage = imageStorage;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {
            createUser("Жүйе әкімшісі", "admin@autorent.kz", adminPassword, Role.ADMIN);
            createUser("Айгерім Менеджер", "manager@autorent.kz", managerPassword, Role.MANAGER);
            createUser("Ерлан Клиентов", "client@autorent.kz", clientPassword, Role.CLIENT);
            log.info("Демо-пайдаланушылар жасалды: admin@autorent.kz, manager@autorent.kz, client@autorent.kz");
        }
        if (carRepository.count() == 0) {
            createCar("Chevrolet", "Cobalt", 2023, "101AAA02", CarCategory.ECONOMY, 12000, 5,
                    "Қала ішінде жүруге үнемді седан. Механика, жанармай шығыны 7 л/100 км.");
            createCar("Hyundai", "Accent", 2022, "202BBB02", CarCategory.ECONOMY, 15000, 5,
                    "Автомат беріліс қорабы, кондиционер, Bluetooth.");
            createCar("Kia", "Rio", 2023, "303CCC01", CarCategory.ECONOMY, 15000, 5,
                    "Ықшам әрі сенімді. Жаңадан бастаған жүргізушілерге ыңғайлы.");
            createCar("Toyota", "Camry 70", 2022, "404DDD02", CarCategory.BUSINESS, 30000, 5,
                    "Бизнес-класс седан. Былғары салон, климат-бақылау.");
            createCar("Hyundai", "Tucson", 2023, "505EEE01", CarCategory.SUV, 28000, 5,
                    "Кроссовер, толық жетек. Қала сыртына сапарға жарайды.");
            createCar("Toyota", "Land Cruiser Prado", 2021, "606FFF02", CarCategory.SUV, 55000, 7,
                    "Нағыз жол талғамайтын көлік. Таулы аймақтарға арналған.");
            createCar("Kia", "Carnival", 2022, "707GGG01", CarCategory.MINIVAN, 45000, 8,
                    "8 орындық минивэн — отбасылық сапарға немесе топқа.");
            createCar("Lexus", "ES 250", 2023, "808HHH02", CarCategory.BUSINESS, 60000, 5,
                    "Премиум-класс. Іссапарлар мен ресми іс-шараларға.");
            log.info("Демо-автопарк жасалды: 8 көлік");
        }
        attachSeedPhotos();
    }

    /** Фотосы жоқ көліктерге seed-photos/ папкасынан фото тіркейді. */
    private void attachSeedPhotos() {
        for (Car car : carRepository.findAll()) {
            if (car.getImageFileName() != null) {
                continue;
            }
            for (String ext : new String[]{".jpg", ".jpeg", ".png", ".webp"}) {
                ClassPathResource resource = new ClassPathResource("seed-photos/" + car.getPlateNumber() + ext);
                if (!resource.exists()) {
                    continue;
                }
                try (InputStream in = resource.getInputStream()) {
                    car.setImageFileName(imageStorage.storeBytes(in.readAllBytes()));
                    log.info("Фото тіркелді: {} ← {}", car.getTitle(), resource.getFilename());
                } catch (IOException | RuntimeException e) {
                    log.warn("Фотоны тіркеу мүмкін болмады ({}): {}", resource.getFilename(), e.getMessage());
                }
                break;
            }
        }
    }

    private void createUser(String name, String email, String rawPassword, Role role) {
        User u = new User();
        u.setFullName(name);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setPhone("+77010000000");
        u.setDriverLicense("KZ123456");
        u.setRole(role);
        userRepository.save(u);
    }

    private void createCar(String brand, String model, int year, String plate, CarCategory category,
                           int price, int seats, String description) {
        Car c = new Car();
        c.setBrand(brand);
        c.setModel(model);
        c.setManufactureYear(year);
        c.setPlateNumber(plate);
        c.setCategory(category);
        c.setPricePerDay(BigDecimal.valueOf(price));
        c.setSeats(seats);
        c.setDescription(description);
        carRepository.save(c);
    }
}
