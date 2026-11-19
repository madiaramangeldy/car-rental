package kz.autorent.web;

import jakarta.validation.Valid;
import kz.autorent.dto.CarForm;
import kz.autorent.exception.BusinessException;
import kz.autorent.exception.InvalidImageException;
import kz.autorent.model.CarCategory;
import kz.autorent.model.CarStatus;
import kz.autorent.model.Role;
import kz.autorent.service.AuditService;
import kz.autorent.service.CarService;
import kz.autorent.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Әкімші панелі: автопарк, пайдаланушылар, аудит журналы. */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final CarService carService;
    private final UserService userService;
    private final AuditService auditService;

    public AdminController(CarService carService, UserService userService, AuditService auditService) {
        this.carService = carService;
        this.userService = userService;
        this.auditService = auditService;
    }

    // ---------- Автопарк ----------

    @GetMapping("/cars")
    public String cars(Model model) {
        model.addAttribute("cars", carService.findAll());
        return "admin/cars";
    }

    @GetMapping("/cars/new")
    public String newCar(Model model) {
        model.addAttribute("carForm", new CarForm());
        addCarFormOptions(model);
        return "admin/car-form";
    }

    @GetMapping("/cars/{id}/edit")
    public String editCar(@PathVariable Long id, Model model) {
        model.addAttribute("carForm", CarForm.from(carService.getById(id)));
        addCarFormOptions(model);
        return "admin/car-form";
    }

    @PostMapping("/cars")
    public String saveCar(@Valid @ModelAttribute("carForm") CarForm form, BindingResult bindingResult,
                          @RequestParam(value = "photo", required = false) MultipartFile photo,
                          @AuthenticationPrincipal UserDetails principal,
                          Model model, RedirectAttributes ra) {
        if (form.getId() != null) {
            // Ағымдағы фотоны формадан емес, дерекқордан аламыз
            form.setCurrentImageFileName(carService.getById(form.getId()).getImageFileName());
        }
        if (bindingResult.hasErrors()) {
            addCarFormOptions(model);
            return "admin/car-form";
        }
        try {
            carService.save(form, photo, principal.getUsername());
        } catch (InvalidImageException e) {
            model.addAttribute("photoError", e.getMessage());
            addCarFormOptions(model);
            return "admin/car-form";
        } catch (BusinessException e) {
            bindingResult.rejectValue("plateNumber", "duplicate", e.getMessage());
            addCarFormOptions(model);
            return "admin/car-form";
        }
        ra.addFlashAttribute("success", "Көлік сақталды.");
        return "redirect:/admin/cars";
    }

    @PostMapping("/cars/{id}/delete")
    public String deleteCar(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal,
                            RedirectAttributes ra) {
        boolean deleted = carService.delete(id, principal.getUsername());
        ra.addFlashAttribute("success", deleted
                ? "Көлік жойылды."
                : "Көліктің брон тарихы бар, сондықтан ол пайдаланудан шығарылды (RETIRED).");
        return "redirect:/admin/cars";
    }

    private void addCarFormOptions(Model model) {
        model.addAttribute("categories", CarCategory.values());
        model.addAttribute("statuses", CarStatus.values());
    }

    // ---------- Пайдаланушылар ----------

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("roles", Role.values());
        return "admin/users";
    }

    @PostMapping("/users/{id}/role")
    public String changeRole(@PathVariable Long id, @RequestParam Role role,
                             @AuthenticationPrincipal UserDetails principal, RedirectAttributes ra) {
        return handleUser(ra, () -> userService.changeRole(id, role, principal.getUsername()), "Рөл өзгертілді.");
    }

    @PostMapping("/users/{id}/toggle")
    public String toggle(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal,
                         RedirectAttributes ra) {
        return handleUser(ra, () -> userService.toggleEnabled(id, principal.getUsername()), "Аккаунт күйі өзгертілді.");
    }

    @PostMapping("/users/{id}/unlock")
    public String unlock(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal,
                         RedirectAttributes ra) {
        return handleUser(ra, () -> userService.unlock(id, principal.getUsername()), "Аккаунт бұғаттан шығарылды.");
    }

    private String handleUser(RedirectAttributes ra, Runnable action, String successMessage) {
        try {
            action.run();
            ra.addFlashAttribute("success", successMessage);
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ---------- Аудит ----------

    @GetMapping("/audit")
    public String audit(Model model) {
        model.addAttribute("logs", auditService.latest());
        return "admin/audit";
    }
}
