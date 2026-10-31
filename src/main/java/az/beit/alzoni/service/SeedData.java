package az.beit.alzoni.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import az.beit.alzoni.domain.Material;
import az.beit.alzoni.domain.MaterialType;
import az.beit.alzoni.domain.Order;
import az.beit.alzoni.domain.OrderType;
import az.beit.alzoni.domain.ProcessRef;
import az.beit.alzoni.domain.Recipe;
import az.beit.alzoni.domain.Role;
import az.beit.alzoni.domain.Warehouse;
import az.beit.alzoni.domain.Workshop;
import jakarta.annotation.PostConstruct;

/**
 * Warehouses and workshops are as listed in protocol PR-2026-000006. Materials, recipes and orders are
 * samples until Alzoni MMC delivers its real lists (protocol section 5).
 */
@Component
public class SeedData {

    private static final String RAW = "Xammal Anbarı";
    private static final String LAMINATE = "Laminasiya (korpus)";
    private static final String SOFT = "Yumşaq";
    private static final String METAL = "Çilingər";
    private static final String PAINT = "Boya sexi";
    private static final String FINISHED = "Hazır Məhsul";
    private static final String BAKU = "Bakı Anbar";

    private final ErpService erp;
    private final UserService users;

    public SeedData(ErpService erp, UserService users) {
        this.erp = erp;
        this.users = users;
    }

    @PostConstruct
    void load() {
        user("Valeh Ağayev", Role.ADMIN, null, null);
        user("Mahir Aliyev", Role.DIREKTOR, null, null);
        user("Nigar Həsənova", Role.SATIS, "+994 50 000 00 01", "nigar@alzoni.example");
        user("Rauf Quliyev", Role.SATIS, "+994 50 000 00 02", "rauf@alzoni.example");
        user("Aysel Kərimova", Role.DIZAYNER, "+994 50 000 00 03", "aysel@alzoni.example");
        user("Elşən Musayev", Role.PLANLAMA, "+994 50 000 00 04", "elshan@alzoni.example");
        user("Səbinə Abbasova", Role.SATINALMA, "+994 50 000 00 05", "sabina@alzoni.example");
        user("Vüqar İsmayılov", Role.ANBAR, "+994 50 000 00 06", "vuqar@alzoni.example");
        user("Orxan Babayev", Role.ISTEHSAL, "+994 50 000 00 07", "orxan@alzoni.example");

        List.of(RAW, LAMINATE, SOFT, METAL, PAINT, FINISHED, BAKU).forEach(n -> erp.addWarehouse(new Warehouse(n)));

        erp.addWorkshop(new Workshop(1, "Sex 1 – Laminasiya (korpus)", List.of("Laminasiya", "Aksesuar", "Güzgü şüşə")));
        erp.addWorkshop(new Workshop(2, "Sex 2 – Yumşaq", List.of("Karkas", "Yumşaq")));
        erp.addWorkshop(new Workshop(3, "Sex 3 – Çilingər sexi", List.of("Çilingər")));
        erp.addWorkshop(new Workshop(4, "Sex 4 – Boya sexi", List.of("Boya")));

        material("XM-001", "Laminat lövhə 18 mm", MaterialType.XAMMAL, "ədəd", RAW, "42.50", "120");
        material("XM-002", "MDF lövhə 16 mm", MaterialType.XAMMAL, "ədəd", RAW, "38.00", "60");
        material("XM-003", "Menteşə", MaterialType.XAMMAL, "ədəd", RAW, "1.80", "400");
        material("XM-004", "Güzgü şüşə 4 mm", MaterialType.XAMMAL, "m²", RAW, "31.00", "14");
        material("XM-005", "Sünger 40 dns", MaterialType.XAMMAL, "m²", RAW, "17.50", "30");
        material("XM-006", "Mebel parçası", MaterialType.XAMMAL, "m", RAW, "12.40", "45");
        material("XM-007", "Metal profil", MaterialType.XAMMAL, "m", RAW, "6.20", "80");
        material("XM-008", "Boya (poliuretan)", MaterialType.XAMMAL, "kq", RAW, "14.90", "18");
        material("XM-009", "Dekorativ aksesuar (xüsusi)", MaterialType.XAMMAL, "ədəd", RAW, null, "0");
        material("YF-001", "Korpus paneli", MaterialType.YARIMFABRIKAT, "ədəd", LAMINATE, "58.00", "16");
        material("YF-002", "Divan karkası", MaterialType.YARIMFABRIKAT, "ədəd", SOFT, "44.00", "8");
        material("YF-003", "Metal ayaq dəsti", MaterialType.YARIMFABRIKAT, "dəst", METAL, "23.00", "20");
        material("HM-001", "Dolab 3 qapılı", MaterialType.HAZIR_MEHSUL, "ədəd", FINISHED, null, "4");
        material("HM-002", "Yumşaq divan", MaterialType.HAZIR_MEHSUL, "ədəd", FINISHED, null, "2");
        material("HM-003", "Metal ayaqlı stol", MaterialType.HAZIR_MEHSUL, "ədəd", BAKU, null, "6");

        recipe("HM-001", "Korpus yığılır, aksesuar və güzgü quraşdırılır.", Map.of("YF-001", "3", "XM-003", "6", "XM-004", "1.5"), "1-1", "1-2", "1-3");
        recipe("HM-002", "Karkas üzərinə sünger və parça çəkilir.", Map.of("YF-002", "1", "XM-005", "3", "XM-006", "6"), "2-1", "2-2");
        recipe("HM-003", "Üst lövhə laminasiya edilir, metal ayaqlar boyanıb bərkidilir.", Map.of("XM-002", "1", "YF-003", "1", "XM-008", "0.5"), "1-1", "3-1", "4-1");
        recipe("YF-001", "Lövhə kəsilir və kənarları laminasiya edilir.", Map.of("XM-001", "1"), "1-1");
        recipe("YF-002", "Metal profildən karkas qaynaqlanır.", Map.of("XM-007", "6"), "2-1");
        recipe("YF-003", "Profil kəsilir, qaynaqlanır və boyanır.", Map.of("XM-007", "4"), "3-1", "4-1");

        LocalDate today = LocalDate.now();

        Order delivered = seri("Bakı Mebel MMC", "Nigar Həsənova", "HM-001", 2);
        toApproved(delivered, 8);
        erp.openProductionOrder(delivered.getNo());
        erp.issueMaterials(delivered.getNo());
        for (int i = 0; i < delivered.getRoute().size(); i++) {
            erp.startStep(delivered.getNo(), i);
            erp.finishStep(delivered.getNo(), i);
        }
        erp.completeProduction(delivered.getNo(), FINISHED);
        erp.deliver(delivered.getNo());

        Order inProduction = seri("Xəzər Otel", "Nigar Həsənova", "HM-002", 2);
        toApproved(inProduction, 10);
        erp.openProductionOrder(inProduction.getNo());
        erp.issueMaterials(inProduction.getNo());
        erp.startStep(inProduction.getNo(), 0);
        erp.finishStep(inProduction.getNo(), 0);
        erp.startStep(inProduction.getNo(), 1);

        Order awaitingGoods = seri("Ofis Evi MMC", "Rauf Quliyev", "HM-003", 30);
        toApproved(awaitingGoods, 14);
        erp.openProductionOrder(awaitingGoods.getNo());
        erp.purchases(awaitingGoods).forEach(p -> erp.placePurchase(p.getNo(), "Metal Təchizat MMC", today.plusDays(3)));

        Order approved = custom(OrderType.OZEL, "Ramil Məmmədov", "Rauf Quliyev", "Fərdi qonaq otağı dəsti", true);
        erp.routeOrder(approved.getNo());
        design(approved, List.of("XM-001", "XM-005", "XM-006"), List.of("6", "4", "8"), List.of("1-1", "1-2", "2-2"));
        toApproved(approved, 12);

        Order costing = custom(OrderType.YENI, "Atrium Dizayn MMC", "Nigar Həsənova", "Resepşn masası", true);
        erp.routeOrder(costing.getNo());
        design(costing, List.of("XM-002", "XM-009", "YF-003"), List.of("5", "4", "2"), List.of("1-1", "1-2", "3-1", "4-1"));

        Order inDesign = custom(OrderType.OZEL, "Leyla Rzayeva", "Rauf Quliyev", "Güzgülü qarderob", false);
        erp.routeOrder(inDesign.getNo());

        seri("Mənzil Plus MMC", "Nigar Həsənova", "HM-001", 1);

        Order stopped = custom(OrderType.YENI, "Tural Əhmədov", "Rauf Quliyev", "Kitab rəfi", false);
        erp.routeOrder(stopped.getNo());
        design(stopped, List.of("XM-002"), List.of("4"), List.of("1-1"));
        cost(stopped, 6);
        erp.decide(stopped.getNo(), false, "Müştəri qiymətlə razılaşmadı.");

        Order waitingApproval = seri("Gənclik Kafe", "Rauf Quliyev", "HM-003", 4);
        erp.routeOrder(waitingApproval.getNo());
        cost(waitingApproval, 7);
    }

    private void user(String name, Role role, String phone, String email) {
        users.create(new UserForm(name, role, phone, email, true));
    }

    private void material(String code, String name, MaterialType type, String unit, String warehouse,
                          String lastPrice, String stock) {
        erp.addMaterial(new Material(code, name, type, unit, erp.warehouse(warehouse),
                lastPrice == null ? null : new BigDecimal(lastPrice), new BigDecimal(stock)));
    }

    private void recipe(String code, String techCard, Map<String, String> lines, String... route) {
        List<ProcessRef> steps = erp.processes().stream().filter(p -> Arrays.asList(route).contains(p.id())).toList();
        List<Recipe.Line> items = lines.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(e -> new Recipe.Line(erp.material(e.getKey()), new BigDecimal(e.getValue()))).toList();
        erp.addRecipe(new Recipe(erp.material(code), techCard, items, steps));
    }

    private Order seri(String customer, String manager, String recipeCode, int qty) {
        return erp.createOrder(new OrderForm(customer, "+994 50 000 00 00", manager, false, OrderType.SERI,
                recipeCode, null, qty, LocalDate.now().plusDays(21), null));
    }

    private Order custom(OrderType type, String customer, String manager, String product, boolean special) {
        return erp.createOrder(new OrderForm(customer, "+994 50 000 00 00", manager, special, type,
                null, product, 1, LocalDate.now().plusDays(30), null));
    }

    private void design(Order order, List<String> codes, List<String> quantities, List<String> route) {
        erp.saveDesign(order.getNo(), "Aysel Kərimova", order.getNo() + ".dwg", "240 × 90 × 60 sm", null, codes,
                quantities.stream().map(BigDecimal::new).toList(), route);
    }

    private void cost(Order order, int days) {
        erp.saveCosting(order.getNo(), Map.of("XM-009", new BigDecimal("15")), BigDecimal.valueOf(25),
                BigDecimal.valueOf(50), days, LocalDate.now().plusDays(2), null);
    }

    private void toApproved(Order order, int days) {
        if (order.getType() == OrderType.SERI) {
            erp.routeOrder(order.getNo());
        }
        cost(order, days);
        erp.decide(order.getNo(), true, null);
    }
}
