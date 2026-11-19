package kz.autorent.exception;

/** Бизнес-ереже бұзылғанда лақтырылады; хабарламасы пайдаланушыға көрсетіледі. */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
