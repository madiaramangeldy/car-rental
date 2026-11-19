package kz.autorent.web;

import kz.autorent.service.ImageStorageService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.Duration;

/** Жүктелген көлік фотоларын беру: /images/cars/{uuid}.jpg */
@Controller
public class ImageController {

    private final ImageStorageService storage;

    public ImageController(ImageStorageService storage) {
        this.storage = storage;
    }

    @GetMapping("/images/cars/{name}")
    public ResponseEntity<Resource> carImage(@PathVariable String name) {
        return storage.find(name)
                .map(path -> ResponseEntity.ok()
                        .contentType(ImageStorageService.mediaType(name))
                        .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic())
                        .body((Resource) new FileSystemResource(path)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
