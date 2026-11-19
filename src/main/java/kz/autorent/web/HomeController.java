package kz.autorent.web;

import kz.autorent.model.Car;
import kz.autorent.model.CarCategory;
import kz.autorent.service.CarService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Controller
public class HomeController {

    private final CarService carService;

    public HomeController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Car> cars = carService.search(null, null, null);
        List<Car> withPhoto = cars.stream().filter(c -> c.getImageFileName() != null).toList();
        List<Car> withoutPhoto = cars.stream().filter(c -> c.getImageFileName() == null).toList();

        // Hero фоны — фотосы бар ең қымбат көлік
        Car heroCar = withPhoto.stream().max(Comparator.comparing(Car::getPricePerDay)).orElse(null);

        // Санаттар: әр санаттан бір өкіл (фотосы барына басымдық)
        List<CategoryTile> tiles = new ArrayList<>();
        for (CarCategory category : CarCategory.values()) {
            List<Car> inCategory = cars.stream().filter(c -> c.getCategory() == category).toList();
            if (inCategory.isEmpty()) {
                continue;
            }
            Car representative = inCategory.stream()
                    .filter(c -> c.getImageFileName() != null)
                    .findFirst()
                    .orElse(inCategory.get(0));
            tiles.add(new CategoryTile(category, representative, inCategory.size()));
        }

        // Танымал: алдымен фотосы барлар
        List<Car> trending = Stream.concat(withPhoto.stream(), withoutPhoto.stream()).limit(4).toList();

        // Промо-баннер: ұзақ сапарға лайық көлік (SUV / минивэн)
        Car promoCar = withPhoto.stream()
                .filter(c -> c.getCategory() == CarCategory.SUV || c.getCategory() == CarCategory.MINIVAN)
                .findFirst()
                .orElse(heroCar);

        model.addAttribute("heroCar", heroCar);
        model.addAttribute("heroImage", findHeroImage());
        model.addAttribute("tiles", tiles);
        model.addAttribute("trending", trending);
        model.addAttribute("promoCar", promoCar);
        model.addAttribute("carCount", cars.size());
        model.addAttribute("categoryCount", tiles.size());
        model.addAttribute("categories", CarCategory.values());
        return "index";
    }

    /**
     * Басты беттің фон суреті: static/img/ ішінде hero.jpg / hero.webp / hero.png болса — сол қолданылады,
     * әйтпесе фотосы бар ең қымбат көліктің суреті.
     */
    private static String findHeroImage() {
        for (String name : new String[]{"hero.jpg", "hero.jpeg", "hero.webp", "hero.png"}) {
            if (new ClassPathResource("static/img/" + name).exists()) {
                return "/img/" + name;
            }
        }
        return null;
    }

    /** Барлық HTTP әдістері үшін (POST сұранысы да осында forward етілуі мүмкін). */
    @RequestMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}
