package kz.autorent.service;

import kz.autorent.model.AuditLog;
import kz.autorent.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/** Қауіпсіздікке қатысты оқиғаларды тіркейді (кіру, бұғаттау, брон, келісімшарт, рөл өзгерту). */
@Service
public class AuditService {

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    /**
     * REQUIRES_NEW: негізгі транзакция кері қайтарылса да (мысалы, AccessDeniedException),
     * аудит жазбасы сақталады.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String username, String action, String details) {
        AuditLog entry = new AuditLog();
        entry.setUsername(truncate(username == null ? "anonymous" : username, 100));
        entry.setAction(truncate(action, 50));
        entry.setDetails(truncate(details, 500));
        entry.setIpAddress(currentIp());
        repository.save(entry);
    }

    public List<AuditLog> latest() {
        return repository.findTop200ByOrderByTimestampDesc();
    }

    private String currentIp() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            return sra.getRequest().getRemoteAddr();
        }
        return "system";
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
