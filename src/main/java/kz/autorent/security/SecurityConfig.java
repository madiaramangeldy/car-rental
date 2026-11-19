package kz.autorent.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * Қауіпсіздік моделі:
 *  - RBAC: CLIENT / MANAGER / ADMIN (URL деңгейінде + @PreAuthorize әдіс деңгейінде)
 *  - Форма арқылы аутентификация, BCrypt
 *  - CSRF қорғанысы (әдепкі бойынша қосулы, Thymeleaf токенді автоматты қосады)
 *  - Сессия фиксациясынан қорғау, HttpOnly + SameSite cookie
 *  - Қауіпсіздік HTTP-тақырыптары: CSP, X-Frame-Options, Referrer-Policy, X-Content-Type-Options
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final LoginSuccessHandler successHandler;
    private final LoginFailureHandler failureHandler;

    public SecurityConfig(LoginSuccessHandler successHandler, LoginFailureHandler failureHandler) {
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/cars", "/cars/*", "/login", "/register",
                        "/access-denied", "/error", "/css/**", "/js/**", "/img/**", "/images/**",
                        "/favicon.ico").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/manager/**").hasAnyRole("MANAGER", "ADMIN")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll())
            .sessionManagement(session -> session
                .sessionFixation(fixation -> fixation.migrateSession()))
            .exceptionHandling(ex -> ex.accessDeniedPage("/access-denied"))
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                        "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data: blob:; "
                        + "form-action 'self'; frame-ancestors 'none'; base-uri 'self'"))
                .frameOptions(frame -> frame.deny())
                .referrerPolicy(ref -> ref.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)));
        return http.build();
    }
}
