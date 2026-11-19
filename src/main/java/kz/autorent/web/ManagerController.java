package kz.autorent.web;

import kz.autorent.exception.BusinessException;
import kz.autorent.model.BookingStatus;
import kz.autorent.service.BookingService;
import kz.autorent.service.ContractService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Менеджер панелі: брондарды өңдеу және келісімшарттар. */
@Controller
@RequestMapping("/manager")
@PreAuthorize("hasAnyRole('MANAGER','ADMIN')") // URL ережесіне қосымша (defense in depth)
public class ManagerController {

    private final BookingService bookingService;
    private final ContractService contractService;

    public ManagerController(BookingService bookingService, ContractService contractService) {
        this.bookingService = bookingService;
        this.contractService = contractService;
    }

    @GetMapping("/bookings")
    public String bookings(@RequestParam(required = false) BookingStatus status, Model model) {
        model.addAttribute("bookings", bookingService.findAll(status));
        model.addAttribute("statuses", BookingStatus.values());
        model.addAttribute("selectedStatus", status);
        return "manager/bookings";
    }

    @PostMapping("/bookings/{id}/confirm")
    public String confirm(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal,
                          RedirectAttributes ra) {
        return handle(ra, () -> bookingService.confirm(id, principal.getUsername()),
                "Брон №" + id + " расталды, келісімшарт жасалды.");
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancel(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal,
                         RedirectAttributes ra) {
        return handle(ra, () -> bookingService.cancelByManager(id, principal.getUsername()),
                "Брон №" + id + " болдырылмады.");
    }

    @PostMapping("/bookings/{id}/complete")
    public String complete(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal,
                           RedirectAttributes ra) {
        return handle(ra, () -> bookingService.complete(id, principal.getUsername()),
                "Брон №" + id + " аяқталды, келісімшарт жабылды.");
    }

    @GetMapping("/contracts")
    public String contracts(Model model) {
        model.addAttribute("contracts", contractService.findAll());
        return "manager/contracts";
    }

    private String handle(RedirectAttributes ra, Runnable action, String successMessage) {
        try {
            action.run();
            ra.addFlashAttribute("success", successMessage);
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manager/bookings";
    }
}
