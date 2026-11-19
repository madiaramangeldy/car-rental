# ==========================================================================
#  GitHub репозиторийін баптау: белгілер (labels), кезеңдер (milestones),
#  әр мүшеге тағайындалған тапсырмалар (issues).
#
#  Іске қосу (System Administrator, репозиторий қалтасында):
#    1) https://cli.github.com орнату, содан кейін:  gh auth login
#    2) Барлық 4 мүше репозиторийге шақыруды қабылдағанына көз жеткізу
#    3) powershell -ExecutionPolicy Bypass -File scripts\github-setup.ps1
# ==========================================================================

$SYSADMIN  = "Kuansh2004"
$DEVELOPER = "madiaramangeldy"
$SECURITY  = "doszhan2005"
$TESTER    = "erkinruslanuly-collab"

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

foreach ($u in @($SYSADMIN, $DEVELOPER, $SECURITY, $TESTER)) {
    if ($u -like "USERNAME_*") {
        Write-Host "Алдымен скрипттің басындағы GitHub логиндерін толтырыңыз!" -ForegroundColor Red
        exit 1
    }
}

# ---------- Labels ----------
Write-Host "`n== Labels ==" -ForegroundColor Cyan
$labels = @(
    @("role: sysadmin",  "5319E7"),
    @("role: developer", "1D76DB"),
    @("role: security",  "B60205"),
    @("role: tester",    "0E8A16"),
    @("task",            "C5DEF5"),
    @("docs",            "FBCA04"),
    @("security",        "D93F0B")
)
foreach ($l in $labels) {
    gh label create $l[0] --color $l[1] --force | Out-Null
    Write-Host "  + $($l[0])"
}

# ---------- Milestones (жоба кезеңдері) ----------
Write-Host "`n== Milestones ==" -ForegroundColor Cyan
$milestones = @(
    "1. Топ құру",
    "2. Талдау",
    "3. Жобалау",
    "4. Іске асыру",
    "5. Тестілеу",
    "6. Қорғау"
)
$existing = gh api 'repos/{owner}/{repo}/milestones?state=all' --jq '.[].title'
foreach ($m in $milestones) {
    if ($existing -notcontains $m) {
        gh api 'repos/{owner}/{repo}/milestones' -f title="$m" | Out-Null
    }
    Write-Host "  + $m"
}

# ---------- Issues ----------
# Формат: тақырып, сипаттама, жауапты, белгілер, кезең
$issues = @(
    # --- System Administrator ---
    @("Репозиторий мен жоба қаңқасын жасау",
      "pom.xml, .gitignore, CarRentalApplication, application.yml, application-postgres.yml, тест конфигурациясы.",
      $SYSADMIN, "task,role: sysadmin", "1. Топ құру"),
    @("GitHub Actions CI баптау",
      "Әр PR-да mvn verify іске қосылатын .github/workflows/ci.yml. main тармағын қорғау (Settings → Branches).",
      $SYSADMIN, "task,role: sysadmin", "1. Топ құру"),
    @("README, CONTRIBUTING және команда құжаты",
      "README.md (іске қосу), CONTRIBUTING.md (git жұмыс тәртібі), docs/05-team-and-defense.md.",
      $SYSADMIN, "docs,role: sysadmin", "1. Топ құру"),
    @("PostgreSQL профилі және демо-стенд",
      "application-postgres.yml тексеру, қорғауға арналған демо-стендті дайындау.",
      $SYSADMIN, "task,role: sysadmin", "6. Қорғау"),

    # --- Security Analyst ---
    @("Пәндік аймақты талдау және талаптар",
      "docs/01-analysis.md: актерлер, FR/NFR, бизнес-ережелер, ұқсас жүйелер, әдебиеттер.",
      $SECURITY, "docs,role: security", "2. Талдау"),
    @("Қатерлер моделі (STRIDE) және OWASP Top 10",
      "docs/03-security.md: активтер, STRIDE кестесі, OWASP сәйкестігі, қорғаныс шаралары.",
      $SECURITY, "docs,security,role: security", "3. Жобалау"),
    @("Spring Security: аутентификация және RBAC",
      "SecurityConfig, CustomUserDetailsService, рөлдер бойынша URL ережелері, CSRF, қауіпсіздік тақырыптары (CSP).",
      $SECURITY, "security,role: security", "4. Іске асыру"),
    @("Brute-force қорғанысы",
      "LoginAttemptService, LoginSuccessHandler, LoginFailureHandler: 5 әрекет → 15 мин бұғаттау, аудит.",
      $SECURITY, "security,role: security", "4. Іске асыру"),
    @("Қауіпсіздік интеграциялық тесттері",
      "SecurityIntegrationTest: рөлдер, CSRF, тақырыптар, кіру (12 тест).",
      $SECURITY, "security,role: security", "5. Тестілеу"),

    # --- Developer ---
    @("Архитектура және деректер моделін жобалау",
      "docs/02-design.md: қабаттар, ER-диаграмма, күй машинасы, URL картасы.",
      $DEVELOPER, "docs,role: developer", "3. Жобалау"),
    @("JPA entity-лері және репозиторийлер",
      "User, Car, Booking, Contract, AuditLog + enum-дар; Spring Data репозиторийлері, existsOverlap сұранысы.",
      $DEVELOPER, "task,role: developer", "4. Іске асыру"),
    @("Сервистер: брондау, келісімшарт, аудит",
      "BookingService, ContractService, CarService, UserService, AuditService, PriceCalculator.",
      $DEVELOPER, "task,role: developer", "4. Іске асыру"),
    @("Контроллерлер және Thymeleaf беттері",
      "Каталог, брондау, менеджер панелі, әкімші панелі (фото жүктеу), келісімшарт беті, CSS/JS, көлік иллюстрациялары.",
      $DEVELOPER, "task,role: developer", "4. Іске асыру"),
    @("Тестілеу кезінде табылған қателерді түзету",
      "Tester ашқан bug-тарды жабу.",
      $DEVELOPER, "task,role: developer", "5. Тестілеу"),

    # --- Tester ---
    @("Тест-жоспар және тест-кейстер",
      "docs/04-testing.md: стратегия, FT (функционалдық) және ST (қауіпсіздік) кейстері.",
      $TESTER, "docs,role: tester", "5. Тестілеу"),
    @("Unit-тесттер: баға және брондау ережелері",
      "PriceCalculatorTest (5), BookingServiceTest (9), ImageStorageServiceTest (6) — JUnit 5 + Mockito.",
      $TESTER, "task,role: tester", "5. Тестілеу"),
    @("Қолмен тестілеу: FT-01…FT-15, ST-01…ST-12",
      "Барлық кейстерді орындау, нәтижені docs/04-testing.md-ге жазу, қателерді Bug issue ретінде ашу.",
      $TESTER, "task,role: tester", "5. Тестілеу"),

    # --- Барлығы ---
    @("Презентация және қорғауға дайындық",
      "Слайдтар (docs/05, 5.4), демо-сценарий репетициясы. Әр мүше өз слайдтарын дайындайды. Үйлестіру — Team Lead.",
      $DEVELOPER, "docs", "6. Қорғау")
)

Write-Host "`n== Issues ==" -ForegroundColor Cyan
foreach ($i in $issues) {
    $url = gh issue create --title $i[0] --body $i[1] --assignee $i[2] --label $i[3] --milestone $i[4]
    Write-Host "  + [$($i[2])] $($i[0])  $url"
}

Write-Host "`nДайын! Тапсырмалар: gh issue list" -ForegroundColor Green
