package kz.autorent.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kz.autorent.service.AuditService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final LoginAttemptService loginAttemptService;
    private final AuditService auditService;

    public LoginSuccessHandler(LoginAttemptService loginAttemptService, AuditService auditService) {
        this.loginAttemptService = loginAttemptService;
        this.auditService = auditService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String email = authentication.getName();
        loginAttemptService.loginSucceeded(email);
        auditService.log(email, "LOGIN_SUCCESS", "Жүйеге кірді");

        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // Рөлге қарай тиісті бетке бағыттау
        String target = "/cars";
        if (roles.contains("ROLE_ADMIN")) {
            target = "/admin/cars";
        } else if (roles.contains("ROLE_MANAGER")) {
            target = "/manager/bookings";
        }
        response.sendRedirect(request.getContextPath() + target);
    }
}
