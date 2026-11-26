# Alzoni ERP – interfeys dizaynı

İstehsal sifarişi iş axınının statik HTML/CSS maketi. Backend yoxdur: bütün səhifələr hazır nümunə
məlumatlarla yazılıb, formalar heç yerə göndərilmir.

Açmaq üçün `index.html` faylını brauzerdə açın və ya qovluğu istənilən statik serverlə paylaşın:

```bash
python -m http.server 4321
```

Sonra http://localhost:4321 ünvanını açın.

| Bölmə | Səhifə |
| --- | --- |
| İş axını | `index.html` |
| Sifarişlər | `orders.html`, statuslar üzrə `orders-<status>.html`, tamamlanma bölməsi `orders.html#completion` |
| Yeni sifariş | `order-new.html` (müştəri və əlaqəli şəxslərin seçimi, "Yeni müştəri" popupu; `?customer=<id>` müştərini əvvəlcədən seçir) |
| Sifariş | `order-<nömrə>.html`, redaktə: `order-<nömrə>-edit.html` |
| Dizayn | `design-SF-2026-0006.html` |
| Kalkulyasiya | `costing-SF-2026-0005.html` |
| Təsdiq | `approval.html`, `approval-SF-2026-0009.html` |
| Xammal təminatı | `supply.html`, `supply-SF-2026-0004.html` |
| Satınalma | `purchasing.html` (mütəxəssis: alırıq + tarix və ya imtina), `purchasing-review.html` (müdir: cavablara baxış) |
| Anbar | `warehouse.html` |
| İstehsal | `production.html`, `production-SF-2026-0002.html` |
| Hesabatlar | `reports.html` (tamamlanmış sifarişlər), `report-<nömrə>.html` (sifarişin kalkulyasiya hesabatı) |
| Məlumatlar | `customers.html`, `customers-huquqi.html`, `customers-fiziki.html`, `customer-<id>.html`, `customer-new.html`, `recipes.html`, `recipe-<kod>.html`, `materials.html` |
| Sistem | `profile.html`, `users.html`, `user-<id>.html`, `user-new.html`, `settings.html` |

Bütün stillər `css/app.css` faylındadır. Müştəri nümunə məlumatları və müştəri/əlaqəli şəxs formalarının davranışı `js/customers.js` faylındadır.
