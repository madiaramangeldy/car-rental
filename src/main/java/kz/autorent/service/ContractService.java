package kz.autorent.service;

import kz.autorent.exception.NotFoundException;
import kz.autorent.model.Booking;
import kz.autorent.model.Contract;
import kz.autorent.model.ContractStatus;
import kz.autorent.repository.ContractRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

@Service
@Transactional
public class ContractService {

    private final ContractRepository contractRepository;
    private final AuditService auditService;

    public ContractService(ContractRepository contractRepository, AuditService auditService) {
        this.contractRepository = contractRepository;
        this.auditService = auditService;
    }

    /** Расталған брон негізінде келісімшарт жасайды. Нөмір форматы: AR-2026-00015. */
    public Contract createFor(Booking booking, String issuedBy) {
        Contract contract = new Contract();
        contract.setBooking(booking);
        contract.setContractNumber(String.format("AR-%d-%05d", Year.now().getValue(), booking.getId()));
        contract.setTotalAmount(booking.getTotalPrice());
        contract.setDeposit(PriceCalculator.deposit(booking.getCar().getPricePerDay()));
        contract.setIssuedBy(issuedBy);
        Contract saved = contractRepository.save(contract);
        booking.setContract(saved);
        auditService.log(issuedBy, "CONTRACT_CREATED", saved.getContractNumber() + " (брон #" + booking.getId() + ")");
        return saved;
    }

    public void close(Booking booking, String actor) {
        contractRepository.findByBooking_Id(booking.getId()).ifPresent(c -> {
            c.setStatus(ContractStatus.CLOSED);
            c.setClosedAt(LocalDateTime.now());
            auditService.log(actor, "CONTRACT_CLOSED", c.getContractNumber());
        });
    }

    public void terminate(Booking booking, String actor) {
        contractRepository.findByBooking_Id(booking.getId()).ifPresent(c -> {
            c.setStatus(ContractStatus.TERMINATED);
            c.setClosedAt(LocalDateTime.now());
            auditService.log(actor, "CONTRACT_TERMINATED", c.getContractNumber());
        });
    }

    @Transactional(readOnly = true)
    public Contract getById(Long id) {
        return contractRepository.findById(id).orElseThrow(() -> new NotFoundException("Келісімшарт табылмады"));
    }

    @Transactional(readOnly = true)
    public List<Contract> findAll() {
        return contractRepository.findAllByOrderByCreatedAtDesc();
    }
}
