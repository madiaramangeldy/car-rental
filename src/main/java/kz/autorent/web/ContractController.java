package kz.autorent.web;

import kz.autorent.model.Contract;
import kz.autorent.service.AuditService;
import kz.autorent.service.ContractService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ContractController {

    private final ContractService contractService;
    private final AuditService auditService;

    public ContractController(ContractService contractService, AuditService auditService) {
        this.contractService = contractService;
        this.auditService = auditService;
    }

    /**
     * Келісімшартты көру. IDOR қорғанысы: клиент тек өз келісімшартын көреді,
     * URL-дегі id-ді өзгертіп бөтен келісімшартты ашу мүмкін емес.
     */
    @GetMapping("/contracts/{id}")
    public String view(@PathVariable Long id, Authentication authentication, Model model) {
        Contract contract = contractService.getById(id);
        boolean staff = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"));
        boolean owner = contract.getBooking().getUser().getEmail().equalsIgnoreCase(authentication.getName());
        if (!staff && !owner) {
            auditService.log(authentication.getName(), "ACCESS_DENIED", "Бөтен келісімшартты ашу әрекеті #" + id);
            throw new AccessDeniedException("Бұл келісімшарт сізге тиесілі емес");
        }
        model.addAttribute("contract", contract);
        model.addAttribute("booking", contract.getBooking());
        return "contracts/view";
    }
}
