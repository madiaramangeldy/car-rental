package kz.autorent.service;

import kz.autorent.exception.InvalidImageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Көлік фотоларын сақтау. Қауіпсіздік шаралары:
 *  - тек JPEG / PNG / WebP; түрі файл атауы немесе Content-Type бойынша емес,
 *    файлдың алғашқы байттары (magic bytes) бойынша анықталады;
 *  - SVG/HTML қабылданбайды (ішінде скрипт болуы мүмкін → XSS);
 *  - көлемі ≤ 5 МБ;
 *  - файл атауы серверде UUID ретінде жасалады — пайдаланушы берген атау ешқашан қолданылмайды
 *    (path traversal "../../" мүмкін емес);
 *  - оқу кезінде атау қатаң үлгімен тексеріледі және папкадан шықпайтыны расталады.
 */
@Service
public class ImageStorageService {

    public static final long MAX_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Pattern SAFE_NAME = Pattern.compile("^[a-f0-9\\-]{36}\\.(jpg|png|webp)$");

    private final Path directory;

    public ImageStorageService(@Value("${app.upload-dir:./uploads}") String uploadDir) {
        this.directory = Paths.get(uploadDir, "cars").toAbsolutePath().normalize();
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new UncheckedIOException("Фото папкасын жасау мүмкін болмады: " + directory, e);
        }
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidImageException("Файл таңдалмаған");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new InvalidImageException("Фото көлемі 5 МБ-тан аспауы керек");
        }
        try {
            return storeBytes(file.getBytes());
        } catch (IOException e) {
            throw new InvalidImageException("Файлды оқу мүмкін болмады");
        }
    }

    public String storeBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new InvalidImageException("Файл бос");
        }
        if (bytes.length > MAX_SIZE_BYTES) {
            throw new InvalidImageException("Фото көлемі 5 МБ-тан аспауы керек");
        }
        String extension = detectExtension(bytes);
        if (extension == null) {
            throw new InvalidImageException("Тек JPEG, PNG немесе WebP суреттер қабылданады");
        }
        String name = UUID.randomUUID() + "." + extension;
        try {
            Files.write(directory.resolve(name), bytes, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            throw new UncheckedIOException("Фотоны сақтау мүмкін болмады", e);
        }
        return name;
    }

    public Optional<Path> find(String name) {
        if (name == null || !SAFE_NAME.matcher(name).matches()) {
            return Optional.empty();
        }
        Path path = directory.resolve(name).normalize();
        if (!path.startsWith(directory) || !Files.isRegularFile(path)) {
            return Optional.empty();
        }
        return Optional.of(path);
    }

    public void delete(String name) {
        find(name).ifPresent(path -> {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
                // Файлды өшіру сәтсіз болса — жұмыс жалғаса береді
            }
        });
    }

    /** Файл түрін алғашқы байттар (сигнатура) бойынша анықтайды. */
    public static String detectExtension(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return "png";
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "webp";
        }
        return null;
    }

    public static MediaType mediaType(String name) {
        if (name.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (name.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        return MediaType.IMAGE_JPEG;
    }
}
