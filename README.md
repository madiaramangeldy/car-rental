# AutoRent — Көлік жалдау жүйесі

Топтық жоба · «Фреймворктар» пәні · 2026

Веб-жүйе үш негізгі процесті автоматтандырады: **автопаркті басқару**, **онлайн брондау** және **жалдау келісімшартын рәсімдеу**.

**Стек:** Java 17 · Spring Boot 3.3 · Spring Data JPA (Hibernate) · Spring Security 6 · Thymeleaf · H2 / PostgreSQL · JUnit 5 + Mockito + MockMvc

---

## 1. Іске қосу

Талаптар: JDK 17+ және Maven (IntelliJ IDEA-да Maven ішінде бар).

**IntelliJ IDEA арқылы:** `File → Open → car-rental` қалтасын ашу → Maven тәуелділіктері жүктелгенше күту → `CarRentalApplication` класын іске қосу (▶).

**Командалық жолдан:**

```bash
mvn spring-boot:run
```

Браузерде ашу: <http://localhost:8080>

Бірінші іске қосқанда дерекқор демо-деректермен толтырылады:

| Рөл      | Email                 | Құпиясөз     |
|----------|-----------------------|--------------|
| Әкімші   | admin@autorent.kz     | Admin123!    |
| Менеджер | manager@autorent.kz   | Manager123!  |
| Клиент   | client@autorent.kz    | Client123!   |

Деректер `./data/autorent.mv.db` файлында сақталады. Таза бастау үшін `data` қалтасын өшіріңіз.

**PostgreSQL-мен:**

```bash
set DB_URL=jdbc:postgresql://localhost:5432/autorent
set DB_USER=postgres
set DB_PASSWORD=...
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

**Тесттер:** `mvn test` (32 тест: бизнес-логика + қауіпсіздік + фото жүктеу).

**Көлік фотолары.** Әкімші панелінде: *Көліктер → Өзгерту → Фото* (JPEG/PNG/WebP, 5 МБ-қа дейін). Демо-көліктерге фотоны бірден қосу үшін файлдарды `src/main/resources/seed-photos/` папкасына мемлекеттік нөмір атауымен салыңыз (мысалы `404DDD02.jpg`) және қосымшаны қайта іске қосыңыз — толығырақ сол папкадағы README-де. Фото жоқ көлікке санатына сай иллюстрация көрсетіледі. Жүктелген фотолар `./uploads/cars/` папкасында сақталады.

---

## 2. Функционалдық мүмкіндіктер

| Рөл | Не істей алады |
|-----|----------------|
| Қонақ | Автопаркті көру, санат пен күндер бойынша бос көліктерді іздеу, тіркелу |
| Клиент | Көлікті брондау, өз брондарын көру, расталмаған бронды болдырмау, өз келісімшартын көру/басып шығару |
| Менеджер | Барлық брондарды көру және сүзу, растау (→ келісімшарт автоматты жасалады), аяқтау (→ келісімшарт жабылады), болдырмау |
| Әкімші | Менеджердің бәрі + көліктерді қосу/өзгерту/жою, көлік фотоларын жүктеу, пайдаланушылар рөлін өзгерту, аккаунтты өшіру/бұғаттан шығару, аудит журналы |

**Бизнес-ережелер:**
- бір көлікке күндері қиылысатын екі белсенді брон болмайды (пессимистік бұғаттау арқылы race condition-нан қорғалған);
- жалдау мерзімі 1–30 тәулік, брон 180 күн бұрын ғана жасалады;
- бір клиентте 3-тен артық расталмаған брон болмайды;
- 7+ тәулікке — 10%, 14+ тәулікке — 15% жеңілдік; кепілдік — 2 тәуліктің құны;
- брон тарихы бар көлік жойылмайды, тек «пайдаланудан шығарылады».

---

## 3. Жоба құрылымы

```
src/main/java/kz/autorent/
├── model/        JPA entity-лері: User, Car, Booking, Contract, AuditLog + enum-дар
├── repository/   Spring Data JPA репозиторийлері
├── service/      Бизнес-логика: BookingService, CarService, ContractService, UserService, AuditService, PriceCalculator
├── security/     SecurityConfig, UserDetailsService, кіру сәтті/сәтсіз өңдеушілері, LoginAttemptService
├── web/          MVC контроллерлер
├── dto/          Формалар + валидация (RegisterForm, CarForm, BookingForm)
├── config/       PasswordEncoder, демо-деректер
└── exception/    BusinessException, NotFoundException
src/main/resources/
├── templates/    Thymeleaf беттері
├── static/       CSS, JS
└── application*.yml
src/test/java/    Unit және интеграциялық тесттер
docs/             Жоба құжаттамасы
.github/          CI (GitHub Actions), PR/Issue шаблондары, CODEOWNERS
scripts/          GitHub-ты баптау скрипті (labels, milestones, issues)
```

## Команда

| Рөл | GitHub |
|-----|--------|
| Developer + Team Lead | [@madiaramangeldy](https://github.com/madiaramangeldy) |
| System Administrator | [@Kuansh2004](https://github.com/Kuansh2004) |
| Security Analyst | [@doszhan2005](https://github.com/doszhan2005) |
| Tester / QA | [@erkinruslanuly-collab](https://github.com/erkinruslanuly-collab) |

---

## 4. Құжаттама

| Файл | Мазмұны | Кезең |
|------|---------|-------|
| [docs/01-analysis.md](docs/01-analysis.md) | Пәндік аймақ, талаптар, ұқсас жүйелерге шолу | 2 — Талдау |
| [docs/02-design.md](docs/02-design.md) | Архитектура, ER-диаграмма, URL картасы, рөлдер матрицасы | 3 — Жобалау |
| [docs/03-security.md](docs/03-security.md) | Қатерлер моделі (STRIDE), OWASP Top 10, қорғаныс шаралары | 3, 5 |
| [docs/04-testing.md](docs/04-testing.md) | Тест-жоспар, тест-кейстер, нәтижелер | 5 — Тестілеу |
| [docs/05-team-and-defense.md](docs/05-team-and-defense.md) | Рөлдер, жұмыс жоспары, үлес, презентация құрылымы | 1, 6 |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Командада GitHub-пен жұмыс тәртібі: тармақтар, коммиттер, PR, review | 1–6 |

---

## 5. Бағалау критерийлеріне сәйкестік

| Критерий | Балл | Жобада қалай көрсетілген |
|----------|------|--------------------------|
| Тақырыпты техникалық ашу | 20 | Автопарк + брондау + келісімшарт толық циклі; Spring Boot, JPA, Security, Thymeleaf |
| Функционалдық (прототип) | 25 | Жұмыс істейтін веб-қосымша, 3 рөл, демо-деректер |
| Қауіпсіздік шешімдері | 15 | RBAC, BCrypt, CSRF, brute-force бұғаттау, IDOR тексеру, CSP, қауіпсіз фото жүктеу, аудит — `docs/03-security.md` |
| Техникалық құжаттама | 10 | `README.md` + `docs/` (5 құжат) |
| Презентация мен қорғау | 10 | `docs/05-team-and-defense.md` — слайдтар жоспары және демо-сценарий |
| Топпен жұмыс | 10 | Рөлдер мен үлес кестесі — `docs/05-team-and-defense.md` |
| Инновация (бонус) | 5 | Пессимистік бұғаттау, жеңілдіктер, автоматты келісімшарт, REQUIRES_NEW аудит |
| Мерзімінде тапсыру | 5 | — |
