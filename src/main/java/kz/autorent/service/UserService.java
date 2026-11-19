package kz.autorent.service;

import kz.autorent.dto.RegisterForm;
import kz.autorent.exception.BusinessException;
import kz.autorent.exception.NotFoundException;
import kz.autorent.model.Role;
import kz.autorent.model.User;
import kz.autorent.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    public User register(RegisterForm form) {
        String email = form.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Бұл email бойынша аккаунт бар");
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new BusinessException("Құпиясөздер сәйкес келмейді");
        }
        User user = new User();
        user.setFullName(form.getFullName().trim());
        user.setEmail(email);
        user.setPhone(form.getPhone());
        user.setDriverLicense(form.getDriverLicense().toUpperCase());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setRole(Role.CLIENT); // Тіркелген кезде тек CLIENT — рөлді тек әкімші өзгерте алады
        User saved = userRepository.save(user);
        auditService.log(email, "REGISTER", "Жаңа клиент тіркелді");
        return saved;
    }

    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("Пайдаланушы табылмады"));
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAllByOrderByCreatedAtDesc();
    }

    public void changeRole(Long userId, Role role, String actorEmail) {
        User user = getById(userId);
        if (user.getEmail().equalsIgnoreCase(actorEmail)) {
            throw new BusinessException("Өз рөліңізді өзгерте алмайсыз");
        }
        Role old = user.getRole();
        user.setRole(role);
        auditService.log(actorEmail, "ROLE_CHANGED", user.getEmail() + ": " + old + " -> " + role);
    }

    public void toggleEnabled(Long userId, String actorEmail) {
        User user = getById(userId);
        if (user.getEmail().equalsIgnoreCase(actorEmail)) {
            throw new BusinessException("Өз аккаунтыңызды өшіре алмайсыз");
        }
        user.setEnabled(!user.isEnabled());
        auditService.log(actorEmail, user.isEnabled() ? "USER_ENABLED" : "USER_DISABLED", user.getEmail());
    }

    public void unlock(Long userId, String actorEmail) {
        User user = getById(userId);
        user.setLockedUntil(null);
        user.setFailedAttempts(0);
        auditService.log(actorEmail, "USER_UNLOCKED", user.getEmail());
    }

    private User getById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("Пайдаланушы табылмады"));
    }
}
