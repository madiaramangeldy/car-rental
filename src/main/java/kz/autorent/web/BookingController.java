package kz.autorent.web;

import jakarta.validation.Valid;
import kz.autorent.dto.BookingForm;
import kz.autorent.exception.BusinessException;
import kz.autorent.model.Booking;
import kz.autorent.model.User;
import kz.autorent.service.BookingService;
import kz.autorent.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Клиенттің брондары. Тек аутентификациядан өткен пайдаланушыларға. */
@Controller
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;

    public BookingController(BookingService bookingService, UserService userService) {
        this.bookingService = bookingService;
        this.userService = userService;
    }

    @PostMapping("/bookings")
    public String create(@Valid @ModelAttribute BookingForm form,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal UserDetails principal,
                         RedirectAttributes redirectAttributes) {
        if (form.getCarId() == null) {
            return "redirect:/cars";
        }
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Брондау күндерін дұрыс толтырыңыз");
            return "redirect:/cars/" + form.getCarId();
        }
        try {
            User user = userService.getByEmail(principal.getUsername());
            Booking booking = bookingService.create(user, form);
            redirectAttributes.addFlashAttribute("success",
                    "Брон №" + booking.getId() + " жасалды. Менеджердің растауын күтіңіз.");
            return "redirect:/bookings/my";
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cars/" + form.getCarId();
        }
    }

    @GetMapping("/bookings/my")
    public String myBookings(@AuthenticationPrincipal UserDetails principal, Model model) {
        User user = userService.getByEmail(principal.getUsername());
        model.addAttribute("bookings", bookingService.findForUser(user));
        return "bookings/my";
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancel(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails principal,
                         RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelByClient(id, userService.getByEmail(principal.getUsername()));
            redirectAttributes.addFlashAttribute("success", "Брон №" + id + " болдырылмады.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/bookings/my";
    }
}
