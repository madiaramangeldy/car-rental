package kz.autorent.web;

import kz.autorent.dto.BookingForm;
import kz.autorent.model.Car;
import kz.autorent.model.CarCategory;
import kz.autorent.model.CarStatus;
import kz.autorent.exception.NotFoundException;
import kz.autorent.service.CarService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/** Ашық каталог (тіркелусіз қолжетімді). */
@Controller
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping("/cars")
    public String list(@RequestParam(required = false) CarCategory category,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                       Model model) {
        model.addAttribute("cars", carService.search(category, from, to));
        model.addAttribute("categories", CarCategory.values());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("today", LocalDate.now());
        return "cars/list";
    }

    @GetMapping("/cars/{id}")
    public String detail(@PathVariable Long id,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                         Model model) {
        Car car = carService.getById(id);
        if (car.getStatus() == CarStatus.RETIRED) {
            throw new NotFoundException("Көлік табылмады");
        }
        BookingForm form = new BookingForm();
        form.setCarId(id);
        form.setStartDate(from);
        form.setEndDate(to);

        model.addAttribute("car", car);
        model.addAttribute("bookingForm", form);
        model.addAttribute("busy", carService.upcomingBookings(id));
        model.addAttribute("today", LocalDate.now());
        return "cars/detail";
    }
}
