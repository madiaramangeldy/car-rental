# Қаріптер (міндетті емес)

Дизайн екі қаріппен жасалған. Файлдар жоқ болса, жүйелік қаріп (Segoe UI) қолданылады — сайт бәрібір дұрыс көрінеді.

| Файл | Қаріп | Қайдан алу |
|------|-------|------------|
| `Unbounded-Bold.woff2` | Unbounded Bold — тақырыптар | fonts.google.com/specimen/Unbounded |
| `Manrope-Variable.woff2` | Manrope — негізгі мәтін | fonts.google.com/specimen/Manrope |

Екеуі де SIL Open Font License бойынша тегін. `.ttf` жүктелсе, https://cloudconvert.com/ttf-to-woff2 арқылы `.woff2`-ге айналдырыңыз да, осы папкаға дәл осы атаулармен салыңыз.

Қаріптер сайттың өз серверінен жүктеледі (CSP `default-src 'self'`), сыртқы CDN қолданылмайды.
