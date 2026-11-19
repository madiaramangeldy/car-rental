# 2. Жобалау: архитектура, деректер моделі, интерфейс

## 2.1. Архитектура

Жүйе классикалық **көпқабатты (layered) архитектурамен** құрылған — Spring MVC + сервер жағында рендеринг (Thymeleaf).

```mermaid
flowchart TB
    B[Браузер] -->|HTTPS, form POST + CSRF токен| F
    subgraph App[Spring Boot қосымшасы]
        F[Spring Security Filter Chain<br/>аутентификация, RBAC, CSRF, тақырыптар]
        F --> C[Контроллерлер — web/<br/>Car, Booking, Manager, Admin, Contract, Auth]
        C --> T[Thymeleaf шаблондары]
        C --> S[Сервистер — service/<br/>бизнес-ережелер, транзакциялар]
        S --> R[Репозиторийлер — Spring Data JPA]
        S --> A[AuditService]
    end
    R --> DB[(H2 / PostgreSQL)]
    A --> DB
```

| Қабат | Жауапкершілігі | Мысал |
|-------|----------------|-------|
| Security | Кім кіре алады, қандай URL-ге | `SecurityConfig`, `LoginFailureHandler` |
| Web (Controller) | HTTP сұранысын қабылдау, валидация, модельді шаблонға беру | `BookingController` |
| Service | Бизнес-ережелер, транзакция шекарасы | `BookingService.create()` |
| Repository | Дерекқорға қол жеткізу | `BookingRepository.existsOverlap()` |
| Model | Домен объектілері (JPA entity) | `Booking`, `Contract` |
| DTO | Форма деректері, валидация | `BookingForm` |

**Неге Thymeleaf, REST + React емес?** Бір deploy-артефакт, CSRF және сессия Spring Security-де дайын, топқа фронтенд-стекті қосымша үйренудің қажеті жоқ. Сервистер қабаты контроллерден тәуелсіз, сондықтан REST API-ді кейін қосу оңай.

## 2.2. Деректер моделі (ER-диаграмма)

```mermaid
erDiagram
    USERS ||--o{ BOOKINGS : "жасайды"
    CARS ||--o{ BOOKINGS : "брондалады"
    BOOKINGS ||--o| CONTRACTS : "негізінде"

    USERS {
        bigint id PK
        varchar full_name
        varchar email UK
        varchar password "BCrypt хэші"
        varchar phone
        varchar driver_license
        varchar role "CLIENT|MANAGER|ADMIN"
        boolean enabled
        int failed_attempts
        timestamp locked_until
    }
    CARS {
        bigint id PK
        varchar brand
        varchar model
        int manufacture_year
        varchar plate_number UK
        varchar category
        decimal price_per_day
        int seats
        varchar status "AVAILABLE|MAINTENANCE|RETIRED"
        varchar image_file_name "UUID.jpg, null → иллюстрация"
        bigint version "оптимистік бұғаттау"
    }
    BOOKINGS {
        bigint id PK
        bigint user_id FK
        bigint car_id FK
        date start_date
        date end_date
        decimal total_price
        varchar status "PENDING|CONFIRMED|COMPLETED|CANCELLED"
    }
    CONTRACTS {
        bigint id PK
        varchar contract_number UK
        bigint booking_id FK
        decimal total_amount
        decimal deposit
        varchar status "ACTIVE|CLOSED|TERMINATED"
        varchar issued_by
    }
    AUDIT_LOG {
        bigint id PK
        timestamp timestamp
        varchar username
        varchar action
        varchar details
        varchar ip_address
    }
```

`bookings(car_id, start_date, end_date)` бойынша индекс — қиылысуды тексеру сұранысын жылдамдатады.

## 2.3. Брон мен келісімшарттың өмірлік циклі

```mermaid
stateDiagram-v2
    [*] --> PENDING: Клиент брондайды
    PENDING --> CONFIRMED: Менеджер растайды / келісімшарт ACTIVE
    PENDING --> CANCELLED: Клиент немесе менеджер болдырмайды
    CONFIRMED --> COMPLETED: Көлік қайтарылды / келісімшарт CLOSED
    CONFIRMED --> CANCELLED: Менеджер болдырмайды / келісімшарт TERMINATED
    COMPLETED --> [*]
    CANCELLED --> [*]
```

## 2.4. Брондау алгоритмі (негізгі сценарий)

```mermaid
sequenceDiagram
    actor K as Клиент
    participant C as BookingController
    participant S as BookingService
    participant CR as CarRepository
    participant BR as BookingRepository
    K->>C: POST /bookings (carId, күндер, CSRF)
    C->>C: @Valid BookingForm
    C->>S: create(user, form)
    S->>S: BR-01, BR-02, BR-06 тексеру
    S->>CR: findByIdForUpdate(carId) — SELECT ... FOR UPDATE
    S->>BR: existsOverlap(carId, start, end)
    alt Қиылысу бар
        S-->>C: BusinessException
        C-->>K: redirect /cars/{id} + қате
    else Бос
        S->>S: PriceCalculator.calculate()
        S->>BR: save(booking PENDING)
        S->>S: audit BOOKING_CREATED
        C-->>K: redirect /bookings/my
    end
```

## 2.5. URL картасы және қол жеткізу матрицасы

| URL | Әдіс | Қонақ | Клиент | Менеджер | Әкімші |
|-----|------|:-----:|:------:|:--------:|:------:|
| `/`, `/cars`, `/cars/{id}` | GET | ✅ | ✅ | ✅ | ✅ |
| `/img/**` (иллюстрациялар), `/images/cars/{uuid}` (жүктелген фотолар) | GET | ✅ | ✅ | ✅ | ✅ |
| `/login`, `/register` | GET/POST | ✅ | — | — | — |
| `/bookings` | POST | ❌ | ✅ | ✅ | ✅ |
| `/bookings/my` | GET | ❌ | ✅ | ✅ | ✅ |
| `/bookings/{id}/cancel` | POST | ❌ | ✅ (тек өзінікі) | ✅ (өзінікі) | ✅ (өзінікі) |
| `/contracts/{id}` | GET | ❌ | ✅ (тек өзінікі) | ✅ | ✅ |
| `/manager/bookings`, `/manager/contracts` | GET | ❌ | ❌ | ✅ | ✅ |
| `/manager/bookings/{id}/confirm\|complete\|cancel` | POST | ❌ | ❌ | ✅ | ✅ |
| `/admin/cars/**` | GET/POST | ❌ | ❌ | ❌ | ✅ |
| `/admin/users/**` | GET/POST | ❌ | ❌ | ❌ | ✅ |
| `/admin/audit` | GET | ❌ | ❌ | ❌ | ✅ |

Тексеру екі деңгейде: URL ережелері (`SecurityConfig`) **және** контроллер класындағы `@PreAuthorize` (defense in depth). Объект деңгейіндегі тексеру («тек өзінікі») — сервисте/контроллерде.

## 2.6. Интерфейс беттері

| Бет | Шаблон |
|-----|--------|
| Басты бет: іздеу формасы, танымал көліктер | `index.html` |
| Автопарк каталогы + сүзгілер | `cars/list.html` |
| Көлік карточкасы + брондау формасы + бос емес күндер | `cars/detail.html` |
| Менің брондарым | `bookings/my.html` |
| Брондарды басқару (менеджер) | `manager/bookings.html` |
| Келісімшарттар тізімі | `manager/contracts.html` |
| Келісімшарт (басып шығаруға дайын) | `contracts/view.html` |
| Автопаркті басқару, көлік формасы | `admin/cars.html`, `admin/car-form.html` |
| Пайдаланушылар, аудит журналы | `admin/users.html`, `admin/audit.html` |
| Кіру, тіркелу, 403, қате | `auth/*.html`, `access-denied.html`, `error.html` |
