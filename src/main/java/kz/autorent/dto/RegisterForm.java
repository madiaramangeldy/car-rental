package kz.autorent.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Тіркелу формасы. Entity-ді тікелей байланыстырмаймыз (mass assignment қорғанысы: рөлді өзгерту мүмкін емес). */
public class RegisterForm {

    @NotBlank(message = "Аты-жөніңізді енгізіңіз")
    @Size(min = 3, max = 100, message = "Аты-жөні 3–100 таңба болуы керек")
    private String fullName;

    @NotBlank(message = "Email енгізіңіз")
    @Email(message = "Email форматы қате")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "Телефон нөмірін енгізіңіз")
    @Pattern(regexp = "^\\+?[0-9]{10,12}$", message = "Телефон форматы: +77011234567")
    private String phone;

    @NotBlank(message = "Жүргізуші куәлігінің нөмірін енгізіңіз")
    @Pattern(regexp = "^[A-Za-z0-9]{6,12}$", message = "Куәлік нөмірі 6–12 әріп/цифр болуы керек")
    private String driverLicense;

    @NotBlank(message = "Құпиясөз енгізіңіз")
    @Size(min = 8, max = 64, message = "Құпиясөз 8–64 таңба болуы керек")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "Құпиясөзде кемінде бір әріп және бір цифр болуы керек")
    private String password;

    @NotBlank(message = "Құпиясөзді қайталаңыз")
    private String confirmPassword;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDriverLicense() { return driverLicense; }
    public void setDriverLicense(String driverLicense) { this.driverLicense = driverLicense; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
