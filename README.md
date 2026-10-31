# Alzoni ERP – İstehsal sifarişi səhifələri

Spring Boot 4.1 (Java 21, Gradle, Spring MVC, Thymeleaf). Səhifələr `Protokol_PR-2026-000006` sənədinə və
"İstehsal Sifarişi İş Axını" sxeminə əsasən hazırlanıb.

```bash
./gradlew bootRun
```

Tətbiq http://localhost:8080 ünvanında açılır. Verilənlər bazası yoxdur: nümunə məlumatlar
`SeedData` sinfindədir, yaddaşda saxlanılır və hər başlatmada sıfırlanır.

| Mərhələ | Səhifə |
| --- | --- |
| İş axını | `/` |
| 1–2. Sifarişin qəbulu və növü | `/orders`, `/orders/new`, `/orders/{no}` |
| 3. Dizayn | `/design`, `/design/{no}`, resept: `/recipes` |
| 4. Kalkulyasiya və planlama | `/costing`, `/costing/{no}` |
| 5. Müştəri və menecer təsdiqi | `/approval`, `/approval/{no}` |
| 6. Xammal təminatı | `/supply`, `/supply/{no}` |
| 7. Satınalma | `/purchasing` |
| 8. Anbar | `/warehouse` |
| 9. İstehsal | `/production`, `/production/{no}` |
| 10. Tamamlanma | `/completion` |
| Məlumatlar | `/recipes`, `/materials`, `/warehouses`, `/workshops` |
| Sistem | `/profile`, `/users`, `/settings` |

Giriş sistemi hələ yoxdur: cari istifadəçi profil səhifəsində seçilir və rollar səhifələrə girişi məhdudlaşdırmır.
