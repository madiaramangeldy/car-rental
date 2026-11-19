package kz.autorent.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kz.autorent.service.AuditService;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final LoginAttemptService loginAttemptService;
    private final AuditService auditService;

    public LoginFailureHandler(LoginAttemptService loginAttemptService, AuditService auditService) {
        this.loginAttemptService = loginAttemptService;
        this.auditService = auditService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String email = request.getParameter("email");
        String target;

        if (exception instanceof LockedException) {
            auditService.log(email, "LOGIN_BLOCKED", "Бұғатталған аккаунтқа кіру әрекеті");
            target = "/login?locked";
        } else if (exception instanceof DisabledException) {
            auditService.log(email, "LOGIN_DISABLED", "Өшірілген аккаунтқа кіру әрекеті");
            target = "/login?disabled";
        } else {
            // Email жоқ па, әлде құпиясөз қате ме — пайдаланушыға бірдей хабарлама (user enumeration қорғанысы)
            loginAttemptService.loginFailed(email);
            auditService.log(email, "LOGIN_FAILED", "Қате email немесе құпиясөз");
            target = "/login?error";
        }
        response.sendRedirect(request.getContextPath() + target);
    }
}
