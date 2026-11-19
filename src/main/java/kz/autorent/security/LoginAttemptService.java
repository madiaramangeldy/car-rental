package kz.autorent.security;

import kz.autorent.repository.UserRepository;
import kz.autorent.service.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** Brute-force қорғанысы: қатарынан 5 қате құпиясөз → аккаунт 15 минутқа бұғатталады. */
@Service
@Transactional
public class LoginAttemptService {

    public static final int MAX_ATTEMPTS = 5;
    public static final int LOCK_MINUTES = 15;

    private final UserRepository userRepository;
    private final AuditService auditService;

    public LoginAttemptService(UserRepository userRepository, AuditService auditService) {
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    public void loginFailed(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        userRepository.findByEmailIgnoreCase(email.trim()).ifPresent(user -> {
            int attempts = user.getFailedAttempts() + 1;
            if (attempts >= MAX_ATTEMPTS) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
                user.setFailedAttempts(0);
                auditService.log(user.getEmail(), "ACCOUNT_LOCKED",
                        MAX_ATTEMPTS + " сәтсіз әрекет, " + LOCK_MINUTES + " минутқа бұғатталды");
            } else {
                user.setFailedAttempts(attempts);
            }
        });
    }

    public void loginSucceeded(String email) {
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            user.setFailedAttempts(0);
            user.setLockedUntil(null);
        });
    }
}
