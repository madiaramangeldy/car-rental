package kz.autorent.repository;

import jakarta.persistence.LockModeType;
import kz.autorent.model.Car;
import kz.autorent.model.CarCategory;
import kz.autorent.model.CarStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {

    List<Car> findByStatusOrderByPricePerDayAsc(CarStatus status);

    List<Car> findByStatusAndCategoryOrderByPricePerDayAsc(CarStatus status, CarCategory category);

    List<Car> findAllByOrderByIdAsc();

    boolean existsByPlateNumberIgnoreCase(String plateNumber);

    /**
     * Пессимистік бұғаттау (SELECT ... FOR UPDATE).
     * Екі клиент бір көлікті бір мезетте брондаса, екіншісі біріншісінің транзакциясы
     * аяқталғанша күтеді — сондықтан "қос брондау" (race condition) болмайды.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Car c where c.id = :id")
    Optional<Car> findByIdForUpdate(@Param("id") Long id);
}
