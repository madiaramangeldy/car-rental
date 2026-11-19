package kz.autorent;

import kz.autorent.exception.InvalidImageException;
import kz.autorent.service.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/** Фото жүктеу қауіпсіздігі: файл түрі, көлемі, атауы, path traversal. */
class ImageStorageServiceTest {

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10, 'J', 'F', 'I', 'F'};

    @TempDir
    Path tempDir;

    private ImageStorageService storage;

    @BeforeEach
    void setUp() {
        storage = new ImageStorageService(tempDir.toString());
    }

    @Test
    void realJpegIsStoredUnderGeneratedName() {
        MockMultipartFile file = new MockMultipartFile("photo", "../../evil name.jpg", "image/jpeg", JPEG);

        String name = storage.store(file);

        assertTrue(name.matches("^[a-f0-9\\-]{36}\\.jpg$"), "Атау UUID болуы керек: " + name);
        assertTrue(storage.find(name).isPresent());
    }

    @Test
    void htmlDisguisedAsJpegIsRejected() {
        MockMultipartFile file = new MockMultipartFile("photo", "photo.jpg", "image/jpeg",
                "<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8));

        assertThrows(InvalidImageException.class, () -> storage.store(file));
    }

    @Test
    void svgIsRejected() {
        MockMultipartFile file = new MockMultipartFile("photo", "car.svg", "image/svg+xml",
                "<svg xmlns=\"http://www.w3.org/2000/svg\"><script>alert(1)</script></svg>".getBytes(StandardCharsets.UTF_8));

        assertThrows(InvalidImageException.class, () -> storage.store(file));
    }

    @Test
    void tooLargeFileIsRejected() {
        byte[] big = Arrays.copyOf(JPEG, (int) ImageStorageService.MAX_SIZE_BYTES + 1);
        MockMultipartFile file = new MockMultipartFile("photo", "big.jpg", "image/jpeg", big);

        assertThrows(InvalidImageException.class, () -> storage.store(file));
    }

    @Test
    void pathTraversalNamesAreNotServed() {
        assertTrue(storage.find("../../application.yml").isEmpty());
        assertTrue(storage.find("..%2F..%2Fsecret.jpg").isEmpty());
        assertTrue(storage.find("photo.jpg").isEmpty());
        assertTrue(storage.find(null).isEmpty());
    }

    @Test
    void pngAndWebpSignaturesAreDetected() {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
        byte[] webp = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'};

        assertEquals("png", ImageStorageService.detectExtension(png));
        assertEquals("webp", ImageStorageService.detectExtension(webp));
        assertNull(ImageStorageService.detectExtension("GIF89a".getBytes(StandardCharsets.US_ASCII)));
    }
}
