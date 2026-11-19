package kz.autorent.service;

import kz.autorent.dto.BookingForm;
import kz.autorent.exception.BusinessException;
import kz.autorent.exception.NotFoundException;
import kz.autorent.model.Booking;
import kz.autorent.model.BookingStatus;
import kz.autorent.model.Car;
import kz.autorent.model.CarStatus;
import kz.autorent.model.User;
import kz.autorent.repository.BookingRepository;
import kz.autorent.repository.CarRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class BookingService {

    public static final int MAX_RENTAL_DAYS = 30;
    public static final int MAX_DAYS_IN_ADVANCE = 180;
    public static final int MAX_PENDING_PER_USER = 3;

    private final BookingRepository bookingRepository;
    private final CarRepository carRepository;
    private final ContractService contractService;
    private final AuditService auditService;

    public BookingService(BookingRepository bookingRepository, CarRepository carRepository,
                          ContractService contractService, AuditService auditService) {
        this.bookingRepository = bookingRepository;
        this.carRepository = carRepository;
        this.contractService = contractService;
        this.auditService = auditService;
    }

    /** Клиент брон жасайды. Барлық бизнес-ережелер сервер жағында тексеріледі. */
    public Booking create(User user, BookingForm form) {
        LocalDate today = LocalDate.now();
        LocalDate start = form.getStartDate();
        LocalDate end = form.getEndDate();

        if (start == null || end == null) {
            throw new BusinessException("Алу және қайтару күндерін таңдаңыз");
        }
        if (start.isBefore(today)) {
            throw new BusinessException("Алу күні өткен күн бола алмайды");
        }
        if (!end.isAfter(start)) {
            throw new BusinessException("Қайтару күні алу күнінен кейін болуы керек");
        }
        long days = ChronoUnit.DAYS.between(start, end);
        if (days > MAX_RENTAL_DAYS) {
            throw new BusinessException("Жалдау мерзімі " + MAX_RENTAL_DAYS + " тәуліктен аспауы керек");
        }
        if (start.isAfter(today.plusDays(MAX_DAYS_IN_ADVANCE))) {
            throw new BusinessException("Брон " + MAX_DAYS_IN_ADVANCE + " күн бұрын ғана жасалады");
        }
        if (bookingRepository.countByUser_IdAndStatus(user.getId(), BookingStatus.PENDING) >= MAX_PENDING_PER_USER) {
            throw new BusinessException("Сізде расталмаған брондар тым көп (макс. " + MAX_PENDING_PER_USER + ")");
        }

        // Көлік жолын бұғаттаймыз: параллель брондау сұраныстары кезекпен өңделеді
        Car car = carRepository.findByIdForUpdate(form.getCarId())
                .orElseThrow(() -> new NotFoundException("Көлік табылмады"));
        if (car.getStatus() != CarStatus.AVAILABLE) {
            throw new BusinessException("Бұл көлік қазір жалға берілмейді");
        }
        if (bookingRepository.existsOverlap(car.getId(), start, end, BookingStatus.ACTIVE)) {
            throw new BusinessException("Таңдалған күндерге бұл көлік бос емес");
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setCar(car);
        booking.setStartDate(start);
        booking.setEndDate(end);
        booking.setComment(form.getComment());
        booking.setTotalPrice(PriceCalculator.calculate(car.getPricePerDay(), days));
        booking.setStatus(BookingStatus.PENDING);
        Booking saved = bookingRepository.save(booking);

        auditService.log(user.getEmail(), "BOOKING_CREATED",
                "#" + saved.getId() + " " + car.getTitle() + " " + start + " — " + end);
        return saved;
    }

    /** Клиент өз бронынан бас тартады (тек PENDING күйінде). IDOR қорғанысы: иесі тексеріледі. */
    public void cancelByClient(Long bookingId, User user) {
        Booking booking = getById(bookingId);
        if (!booking.getUser().getId().equals(user.getId())) {
            auditService.log(user.getEmail(), "ACCESS_DENIED", "Бөтен бронды болдырмау әрекеті #" + bookingId);
            throw new AccessDeniedException("Бұл брон сізге тиесілі емес");
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException("Тек расталмаған бронды болдырмауға болады");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        auditService.log(user.getEmail(), "BOOKING_CANCELLED", "#" + bookingId + " (клиент)");
    }

    /** Менеджер растайды → келісімшарт автоматты түрде жасалады. */
    public Booking confirm(Long bookingId, String managerEmail) {
        Booking booking = getById(bookingId);
        requireStatus(booking, BookingStatus.PENDING, "Тек күтудегі бронды растауға болады");
        booking.setStatus(BookingStatus.CONFIRMED);
        contractService.createFor(booking, managerEmail);
        auditService.log(managerEmail, "BOOKING_CONFIRMED", "#" + bookingId);
        return booking;
    }

    public void cancelByManager(Long bookingId, String managerEmail) {
        Booking booking = getById(bookingId);
        if (!BookingStatus.ACTIVE.contains(booking.getStatus())) {
            throw new BusinessException("Бұл бронды болдырмау мүмкін емес");
        }
        boolean hadContract = booking.getStatus() == BookingStatus.CONFIRMED;
        booking.setStatus(BookingStatus.CANCELLED);
        if (hadContract) {
            contractService.terminate(booking, managerEmail);
        }
        auditService.log(managerEmail, "BOOKING_CANCELLED", "#" + bookingId + " (менеджер)");
    }

    /** Көлік қайтарылды → брон аяқталады, келісімшарт жабылады. */
    public void complete(Long bookingId, String managerEmail) {
        Booking booking = getById(bookingId);
        requireStatus(booking, BookingStatus.CONFIRMED, "Тек расталған бронды аяқтауға болады");
        booking.setStatus(BookingStatus.COMPLETED);
        contractService.close(booking, managerEmail);
        auditService.log(managerEmail, "BOOKING_COMPLETED", "#" + bookingId);
    }

    @Transactional(readOnly = true)
    public List<Booking> findForUser(User user) {
        return bookingRepository.findByUser_IdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional(readOnly = true)
    public List<Booking> findAll(BookingStatus status) {
        return status == null
                ? bookingRepository.findAllByOrderByCreatedAtDesc()
                : bookingRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    private Booking getById(Long id) {
        return bookingRepository.findById(id).orElseThrow(() -> new NotFoundException("Брон табылмады"));
    }

    private static void requireStatus(Booking booking, BookingStatus expected, String message) {
        if (booking.getStatus() != expected) {
            throw new BusinessException(message);
        }
    }
}
