# GitHub-пен командалық жұмыс тәртібі

Репозиторий: **https://github.com/madiaramangeldy/car-rental**

Әр мүше **өз GitHub аккаунтынан** өз бөлігін коммиттейді және Pull Request (PR) арқылы `main`-ге қосады. Сонда әр адамның үлесі GitHub-та көрінеді: *Insights → Contributors*, PR-лар, Issues.

| # | Кім | Рөл | Тармақ (branch) | Папка (`team-split/`) | PR-ды кім тексереді |
|---|-----|-----|-----------------|-----------------------|---------------------|
| 1 | @Kuansh2004 | System Administrator | `main` (бірінші push) | `1-sysadmin` | — |
| 2 | @madiaramangeldy | Developer + Team Lead | `feature/core` | `2-developer` | @Kuansh2004, @doszhan2005 |
| 3 | @doszhan2005 | Security Analyst | `feature/security` | `3-security` | @madiaramangeldy |
| 4 | @erkinruslanuly-collab | Tester / QA | `feature/tests` | `4-tester` | @madiaramangeldy |
| 5 | @madiaramangeldy | Developer (bug fix) | `fix/audit-rollback` | `5-developer-fix` | @erkinruslanuly-collab |

PR-ларды `main`-ге **Team Lead (@madiaramangeldy)** біріктіреді (*Merge pull request*), өз PR-ын — басқалар Approve бергеннен кейін.

**Рет маңызды:** әр келесі қадам алдыңғы PR `main`-ге біріктірілгеннен кейін басталады (кодтың бір бөлігі алдыңғысына тәуелді).

---

## 0. Барлығы: бір рет баптау

1. Репозиторийге шақыруды қабылдау: email-дегі хат немесе https://github.com/madiaramangeldy/car-rental/invitations → **Accept invitation**.
2. **Git for Windows** орнату: https://git-scm.com (баптауларды әдепкідей қалдыру).
3. **Git Bash** ашып:

```bash
git config --global user.name  "Аты Жөні"
git config --global user.email "github-тағы-email@example.com"
```

> ⚠️ Email GitHub аккаунтындағы email-мен **бірдей** болуы керек, әйтпесе коммиттер профильге есептелмейді.

4. `car-rental-github.zip` архивін (Team Lead жібереді) мысалы `C:\team\` ішіне ашу → `C:\team\team-split\` пайда болады.

Бірінші `git push` кезінде браузерде GitHub-қа кіру терезесі ашылады — өз аккаунтыңызбен кіресіз.

---

## 1. System Administrator — @Kuansh2004

```bash
cd /c/team
git clone https://github.com/madiaramangeldy/car-rental.git
cd car-rental
cp -r ../team-split/1-sysadmin/. .

git add pom.xml .gitignore src/main/java/kz/autorent/CarRentalApplication.java
git commit -m "chore: Spring Boot жоба қаңқасы"

git add src/main/resources src/test/resources
git commit -m "chore: H2 және PostgreSQL конфигурациясы"

git add .github
git commit -m "ci: GitHub Actions, PR/Issue шаблондары, CODEOWNERS"

git add README.md CONTRIBUTING.md docs scripts
git commit -m "docs: README, жұмыс тәртібі, команда және қорғау жоспары"

git push -u origin main
```

**Issues жасау** (барлығы шақыруды қабылдағаннан кейін): https://cli.github.com орнату → PowerShell-де `gh auth login` → репозиторий қалтасында:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\github-setup.ps1
```

Скрипт 6 кезеңді (milestones), белгілерді және әр мүшеге тағайындалған 18 тапсырманы жасайды. Өзіңіздің №1–3 тапсырмаларыңызды осыдан кейін жабыңыз (*Close issue*).

Соңында Team Lead-ке «дайын» деп хабарлаңыз.

---

## 2. Developer + Team Lead — @madiaramangeldy

SysAdmin push жасағаннан кейін:

```bash
cd /c/team
git clone https://github.com/madiaramangeldy/car-rental.git
cd car-rental
git checkout -b feature/core
cp -r ../team-split/2-developer/. .
```

**Фотоларды қосу:** өз жұмыс жобаңыздан көшіріңіз:
- көлік фотолары → `src/main/resources/seed-photos/` (атауы — нөмір: `404DDD02.jpg` т.с.с.);
- `src/main/resources/static/img/hero.jpg`.

```bash
git add src/main/java/kz/autorent/model
git commit -m "feat: домен моделі (User, Car, Booking, Contract, AuditLog)"

git add src/main/java/kz/autorent/repository
git commit -m "feat: JPA репозиторийлері, күндердің қиылысу сұранысы"

git add src/main/java/kz/autorent/dto src/main/java/kz/autorent/exception src/main/java/kz/autorent/config
git commit -m "feat: DTO формалары, валидация, демо-деректер"

git add src/main/java/kz/autorent/service
git commit -m "feat: брондау, келісімшарт, фото, аудит сервистері"

git add src/main/java/kz/autorent/web
git commit -m "feat: MVC контроллерлер"

git add src/main/resources/templates src/main/resources/static/css src/main/resources/static/js
git commit -m "feat: Thymeleaf беттері, дизайн және анимациялар"

git add src/main/resources/static src/main/resources/seed-photos
git commit -m "feat: көлік фотолары мен иллюстрациялары"

git add docs/02-design.md
git commit -m "docs: архитектура және деректер моделі"

git push -u origin feature/core
```

GitHub → **Compare & pull request** → сипаттамаға `Closes #10, Closes #11, Closes #12, Closes #13` → **Create pull request**. @Kuansh2004 мен @doszhan2005 *Approve* бергеннен кейін → **Merge pull request**.

---

## 3. Security Analyst — @doszhan2005

Developer PR-ы біріктірілгеннен кейін:

```bash
cd /c/team
git clone https://github.com/madiaramangeldy/car-rental.git
cd car-rental
git checkout -b feature/security
cp -r ../team-split/3-security/. .

git add docs/01-analysis.md
git commit -m "docs: пәндік аймақты талдау және талаптар"

git add docs/03-security.md
git commit -m "docs: STRIDE қатерлер моделі, OWASP Top 10"

git add src/main/java/kz/autorent/security/SecurityConfig.java src/main/java/kz/autorent/security/CustomUserDetailsService.java
git commit -m "feat(security): аутентификация, RBAC, CSRF, CSP"

git add src/main/java/kz/autorent/security
git commit -m "feat(security): brute-force қорғанысы, кіру аудиті"

git add src/test/java/kz/autorent/SecurityIntegrationTest.java
git commit -m "test(security): рөлдер, CSRF, тақырыптар тесттері"

git push -u origin feature/security
```

PR сипаттамасы: `Closes #5, Closes #6, Closes #7, Closes #8, Closes #9`.

Сонымен қатар Developer PR-ын **қауіпсіздік тұрғысынан** тексеріп, *Files changed → Review changes → Approve* (PR шаблонындағы чек-лист бойынша).

---

## 4. Tester / QA — @erkinruslanuly-collab

Security PR біріктірілгеннен кейін:

```bash
cd /c/team
git clone https://github.com/madiaramangeldy/car-rental.git
cd car-rental
git checkout -b feature/tests
cp -r ../team-split/4-tester/. .

git add docs/04-testing.md
git commit -m "docs: тест-жоспар, FT және ST кейстері"

git add src/test/java/kz/autorent/PriceCalculatorTest.java
git commit -m "test: баға есептеу тесттері"

git add src/test/java/kz/autorent/BookingServiceTest.java
git commit -m "test: брондау ережелерінің тесттері"

git add src/test/java/kz/autorent/ImageStorageServiceTest.java
git commit -m "test: фото жүктеу қауіпсіздігінің тесттері"

git push -u origin feature/tests
```

PR сипаттамасы: `Closes #15, Closes #16`.

PR біріктірілгеннен кейін:
1. Жобаны IntelliJ-те ашып, іске қосыңыз, `docs/04-testing.md`-дегі FT және ST кейстерін орындаңыз.
2. **ST-13** кейсі қате береді: 403 шығады, бірақ аудитте `ACCESS_DENIED` жоқ → *Issues → New issue → Қате туралы хабарлама* → Assignee: @madiaramangeldy.
3. Нәтижелерді (✅/❌) `docs/04-testing.md`-ге жазып, жаңа тармақта коммит + PR жасаңыз.

---

## 5. Developer — қатені түзету (@madiaramangeldy)

```bash
cd /c/team/car-rental
git checkout main && git pull
git checkout -b fix/audit-rollback
cp -r ../team-split/5-developer-fix/. .

git add src/main/java/kz/autorent/service/AuditService.java
git commit -m "fix: аудит жазбасы транзакция кері қайтарылғанда да сақталады (REQUIRES_NEW)"
git push -u origin fix/audit-rollback
```

PR сипаттамасы: `Closes #<bug нөмірі>`. @erkinruslanuly-collab ST-13-ті қайта тексеріп *Approve* басады → Merge.

---

## Коммит хабарламалары

| Префикс | Қашан |
|---------|-------|
| `feat:` | Жаңа функция |
| `fix:` | Қатені түзету |
| `docs:` | Құжаттама |
| `test:` | Тесттер |
| `ci:` / `chore:` | CI, конфигурация, қызметтік өзгерістер |

## Ережелер

- `main`-ге тікелей push жасамаймыз (тек SysAdmin-нің бірінші push-ы) — бәрі PR арқылы.
- Бір PR — бір тапсырма. Сипаттамада `Closes #N`.
- CI қызыл болса — біріктірмейміз.
- `data/`, `target/`, `uploads/`, `.idea/`, құпиясөздер коммитке кірмейді (`.gitignore`).
- Қорғау алдында: `git shortlog -sn` — әркімнің коммит саны.
