package kz.autorent;

import kz.autorent.dto.BookingForm;
import kz.autorent.exception.BusinessException;
import kz.autorent.model.*;
import kz.autorent.repository.BookingRepository;
import kz.autorent.repository.CarRepository;
import kz.autorent.service.AuditService;
import kz.autorent.service.BookingService;
import kz.autorent.service.ContractService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock BookingRepository bookingRepository;
    @Mock CarRepository carRepository;
    @Mock ContractService contractService;
    @Mock AuditService auditService;

    @InjectMocks BookingService bookingService;

    private User client;
    private Car car;

    @BeforeEach
    void setUp() {
        client = new User();
        client.setId(1L);
        client.setEmail("client@test.kz");

        car = new Car();
        car.setId(10L);
        car.setBrand("Kia");
        car.setModel("Rio");
        car.setPricePerDay(new BigDecimal("15000"));
        car.setStatus(CarStatus.AVAILABLE);
    }

    private BookingForm form(LocalDate start, LocalDate end) {
        BookingForm f = new BookingForm();
        f.setCarId(10L);
        f.setStartDate(start);
        f.setEndDate(end);
        return f;
    }

    @Test
    void pastStartDateIsRejected() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        assertThrows(BusinessException.class,
                () -> bookingService.create(client, form(yesterday, yesterday.plusDays(3))));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void endBeforeStartIsRejected() {
        LocalDate start = LocalDate.now().plusDays(5);
        assertThrows(BusinessException.class,
                () -> bookingService.create(client, form(start, start.minusDays(1))));
    }

    @Test
    void tooLongRentalIsRejected() {
        LocalDate start = LocalDate.now().plusDays(1);
        assertThrows(BusinessException.class,
                () -> bookingService.create(client, form(start, start.plusDays(31))));
    }

    @Test
    void overlappingBookingIsRejected() {
        LocalDate start = LocalDate.now().plusDays(2);
        when(carRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(car));
        when(bookingRepository.existsOverlap(eq(10L), any(), any(), anyCollection())).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> bookingService.create(client, form(start, start.plusDays(3))));
        assertTrue(ex.getMessage().contains("бос емес"));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void carInMaintenanceIsRejected() {
        car.setStatus(CarStatus.MAINTENANCE);
        LocalDate start = LocalDate.now().plusDays(2);
        when(carRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(car));

        assertThrows(BusinessException.class,
                () -> bookingService.create(client, form(start, start.plusDays(3))));
    }

    @Test
    void validBookingIsCreatedWithCorrectPrice() {
        LocalDate start = LocalDate.now().plusDays(2);
        when(carRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(car));
        when(bookingRepository.existsOverlap(eq(10L), any(), any(), anyCollection())).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking booking = bookingService.create(client, form(start, start.plusDays(3)));

        assertEquals(BookingStatus.PENDING, booking.getStatus());
        assertEquals(new BigDecimal("45000.00"), booking.getTotalPrice());
        assertEquals(client, booking.getUser());
    }

    @Test
    void clientCannotCancelSomeoneElsesBooking() {
        User other = new User();
        other.setId(2L);
        Booking booking = new Booking();
        booking.setId(5L);
        booking.setUser(other);
        booking.setStatus(BookingStatus.PENDING);
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));

        assertThrows(AccessDeniedException.class, () -> bookingService.cancelByClient(5L, client));
        assertEquals(BookingStatus.PENDING, booking.getStatus());
    }

    @Test
    void confirmCreatesContract() {
        Booking booking = new Booking();
        booking.setId(7L);
        booking.setCar(car);
        booking.setStatus(BookingStatus.PENDING);
        when(bookingRepository.findById(7L)).thenReturn(Optional.of(booking));

        bookingService.confirm(7L, "manager@test.kz");

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        verify(contractService).createFor(booking, "manager@test.kz");
    }

    @Test
    void cannotCompletePendingBooking() {
        Booking booking = new Booking();
        booking.setId(8L);
        booking.setStatus(BookingStatus.PENDING);
        when(bookingRepository.findById(8L)).thenReturn(Optional.of(booking));

        assertThrows(BusinessException.class, () -> bookingService.complete(8L, "manager@test.kz"));
    }
}
