package kz.autorent.repository;

import kz.autorent.model.Booking;
import kz.autorent.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Көрсетілген кезеңмен қиылысатын белсенді брон бар ма?
     * Кезеңдер [start, end) ретінде қарастырылады: қайтару күні келесі клиент ала алады.
     * Параметрлер JPQL арқылы байланыстырылады — SQL-инъекция мүмкін емес.
     */
    @Query("""
            select case when count(b) > 0 then true else false end
            from Booking b
            where b.car.id = :carId
              and b.status in :statuses
              and b.startDate < :endDate
              and b.endDate > :startDate
            """)
    boolean existsOverlap(@Param("carId") Long carId,
                          @Param("startDate") LocalDate startDate,
                          @Param("endDate") LocalDate endDate,
                          @Param("statuses") Collection<BookingStatus> statuses);

    List<Booking> findByUser_IdOrderByCreatedAtDesc(Long userId);

    List<Booking> findAllByOrderByCreatedAtDesc();

    List<Booking> findByStatusOrderByCreatedAtDesc(BookingStatus status);

    long countByUser_IdAndStatus(Long userId, BookingStatus status);

    boolean existsByCar_Id(Long carId);

    /** Көліктің алдағы бос емес кезеңдері (каталогта көрсету үшін). */
    List<Booking> findByCar_IdAndStatusInAndEndDateAfterOrderByStartDateAsc(
            Long carId, Collection<BookingStatus> statuses, LocalDate date);
}
