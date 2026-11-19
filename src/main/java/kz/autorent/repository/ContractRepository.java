package kz.autorent.repository;

import kz.autorent.model.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Long> {

    List<Contract> findAllByOrderByCreatedAtDesc();

    Optional<Contract> findByBooking_Id(Long bookingId);
}
