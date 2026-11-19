package kz.autorent;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Қауіпсіздік тесттері: рөлдер, CSRF, тақырыптар, аутентификация. */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void publicCatalogIsAccessibleWithoutLogin() throws Exception {
        mvc.perform(get("/cars")).andExpect(status().isOk());
    }

    @Test
    void myBookingsRequiresLogin() throws Exception {
        mvc.perform(get("/bookings/my"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "client@autorent.kz", roles = "CLIENT")
    void clientCannotAccessAdminPanel() throws Exception {
        mvc.perform(get("/admin/cars")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "client@autorent.kz", roles = "CLIENT")
    void clientCannotAccessManagerPanel() throws Exception {
        mvc.perform(get("/manager/bookings")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager@autorent.kz", roles = "MANAGER")
    void managerCannotAccessAdminPanel() throws Exception {
        mvc.perform(get("/admin/users")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@autorent.kz", roles = "ADMIN")
    void adminCanAccessAdminPanel() throws Exception {
        mvc.perform(get("/admin/cars")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "client@autorent.kz", roles = "CLIENT")
    void postWithoutCsrfTokenIsRejected() throws Exception {
        mvc.perform(post("/bookings").param("carId", "1")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "client@autorent.kz", roles = "CLIENT")
    void postWithCsrfTokenIsAccepted() throws Exception {
        mvc.perform(post("/bookings").with(csrf()).param("carId", "1"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void securityHeadersArePresent() throws Exception {
        mvc.perform(get("/"))
                .andExpect(header().exists("Content-Security-Policy"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void carIllustrationsArePublicAndInvalidPhotoNamesReturn404() throws Exception {
        mvc.perform(get("/img/cars/economy.svg")).andExpect(status().isOk());
        mvc.perform(get("/images/cars/not-a-uuid.jpg")).andExpect(status().isNotFound());
    }

    @Test
    void loginWithWrongPasswordFails() throws Exception {
        mvc.perform(formLogin("/login").userParameter("email").user("manager@autorent.kz").password("wrong"))
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void loginWithCorrectPasswordRedirectsByRole() throws Exception {
        mvc.perform(formLogin("/login").userParameter("email").user("client@autorent.kz").password("Client123!"))
                .andExpect(redirectedUrl("/cars"));
        mvc.perform(formLogin("/login").userParameter("email").user("admin@autorent.kz").password("Admin123!"))
                .andExpect(redirectedUrl("/admin/cars"));
    }
}
