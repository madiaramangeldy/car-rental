package kz.autorent.service;

import kz.autorent.dto.CarForm;
import kz.autorent.exception.BusinessException;
import kz.autorent.exception.NotFoundException;
import kz.autorent.model.Booking;
import kz.autorent.model.BookingStatus;
import kz.autorent.model.Car;
import kz.autorent.model.CarCategory;
import kz.autorent.model.CarStatus;
import kz.autorent.repository.BookingRepository;
import kz.autorent.repository.CarRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class CarService {

    private final CarRepository carRepository;
    private final BookingRepository bookingRepository;
    private final AuditService auditService;
    private final ImageStorageService imageStorage;

    public CarService(CarRepository carRepository, BookingRepository bookingRepository,
                      AuditService auditService, ImageStorageService imageStorage) {
        this.carRepository = carRepository;
        this.bookingRepository = bookingRepository;
        this.auditService = auditService;
        this.imageStorage = imageStorage;
    }

    /** Каталог: қолжетімді көліктер, санат және күндер бойынша сүзгі. */
    @Transactional(readOnly = true)
    public List<Car> search(CarCategory category, LocalDate from, LocalDate to) {
        List<Car> cars = (category == null)
                ? carRepository.findByStatusOrderByPricePerDayAsc(CarStatus.AVAILABLE)
                : carRepository.findByStatusAndCategoryOrderByPricePerDayAsc(CarStatus.AVAILABLE, category);
        if (from != null && to != null && to.isAfter(from)) {
            cars = cars.stream()
                    .filter(c -> !bookingRepository.existsOverlap(c.getId(), from, to, BookingStatus.ACTIVE))
                    .toList();
        }
        return cars;
    }

    @Transactional(readOnly = true)
    public Car getById(Long id) {
        return carRepository.findById(id).orElseThrow(() -> new NotFoundException("Көлік табылмады"));
    }

    @Transactional(readOnly = true)
    public List<Car> findAll() {
        return carRepository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public List<Booking> upcomingBookings(Long carId) {
        return bookingRepository.findByCar_IdAndStatusInAndEndDateAfterOrderByStartDateAsc(
                carId, BookingStatus.ACTIVE, LocalDate.now());
    }

    public Car save(CarForm form, MultipartFile photo, String actorEmail) {
        String plate = form.getPlateNumber().trim().toUpperCase();
        Car car;
        if (form.getId() == null) {
            if (carRepository.existsByPlateNumberIgnoreCase(plate)) {
                throw new BusinessException("Бұл мемлекеттік нөмірмен көлік бар");
            }
            car = new Car();
        } else {
            car = getById(form.getId());
            if (!car.getPlateNumber().equalsIgnoreCase(plate) && carRepository.existsByPlateNumberIgnoreCase(plate)) {
                throw new BusinessException("Бұл мемлекеттік нөмірмен көлік бар");
            }
        }
        car.setBrand(form.getBrand().trim());
        car.setModel(form.getModel().trim());
        car.setManufactureYear(form.getManufactureYear());
        car.setPlateNumber(plate);
        car.setCategory(form.getCategory());
        car.setPricePerDay(form.getPricePerDay());
        car.setSeats(form.getSeats());
        car.setStatus(form.getStatus());
        car.setDescription(form.getDescription());

        // Фото: жаңасы жүктелсе — ауыстырамыз, "өшіру" белгіленсе — алып тастаймыз
        String oldImage = car.getImageFileName();
        if (photo != null && !photo.isEmpty()) {
            car.setImageFileName(imageStorage.store(photo));
        } else if (form.isRemovePhoto()) {
            car.setImageFileName(null);
        }
        Car saved = carRepository.save(car);
        if (oldImage != null && !oldImage.equals(saved.getImageFileName())) {
            imageStorage.delete(oldImage);
        }
        auditService.log(actorEmail, form.getId() == null ? "CAR_CREATED" : "CAR_UPDATED",
                saved.getTitle() + " " + saved.getPlateNumber());
        return saved;
    }

    /**
     * Броны жоқ көлік толығымен жойылады; тарихы бар көлік тек "пайдаланудан шығарылады"
     * (келісімшарттар тарихы сақталуы үшін).
     * @return true — жойылды, false — RETIRED статусына ауыстырылды
     */
    public boolean delete(Long id, String actorEmail) {
        Car car = getById(id);
        if (bookingRepository.existsByCar_Id(id)) {
            car.setStatus(CarStatus.RETIRED);
            auditService.log(actorEmail, "CAR_RETIRED", car.getTitle() + " " + car.getPlateNumber());
            return false;
        }
        carRepository.delete(car);
        imageStorage.delete(car.getImageFileName());
        auditService.log(actorEmail, "CAR_DELETED", car.getTitle() + " " + car.getPlateNumber());
        return true;
    }
}
