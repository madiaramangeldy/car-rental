package kz.autorent.exception;

/** Жүктелген файл фото талаптарына сай емес. */
public class InvalidImageException extends BusinessException {

    public InvalidImageException(String message) {
        super(message);
    }
}
