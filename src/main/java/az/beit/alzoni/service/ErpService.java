package az.beit.alzoni.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Predicate;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import az.beit.alzoni.domain.Material;
import az.beit.alzoni.domain.MaterialType;
import az.beit.alzoni.domain.Order;
import az.beit.alzoni.domain.OrderLine;
import az.beit.alzoni.domain.OrderStatus;
import az.beit.alzoni.domain.ProcessRef;
import az.beit.alzoni.domain.PurchaseRequest;
import az.beit.alzoni.domain.PurchaseStatus;
import az.beit.alzoni.domain.Recipe;
import az.beit.alzoni.domain.Requirement;
import az.beit.alzoni.domain.Role;
import az.beit.alzoni.domain.RouteStep;
import az.beit.alzoni.domain.Settings;
import az.beit.alzoni.domain.Task;
import az.beit.alzoni.domain.Warehouse;
import az.beit.alzoni.domain.Workshop;

/** In-memory stand-in for the real ERP: holds the data behind the pages and moves orders through the flow. */
@Service
public class ErpService {

    private static final Map<String, String[]> TASK_TARGETS = Map.of(
            "orders", new String[] {"Yönləndirilməli sifarişlər", "/orders?status=YENI"},
            "design", new String[] {"Dizayn gözləyir", "/design"},
            "costing", new String[] {"Kalkulyasiya gözləyir", "/costing"},
            "approval", new String[] {"Təsdiq gözləyir", "/approval"},
            "supply", new String[] {"İstehsal sifarişi açılmalıdır", "/supply"},
            "purchasing", new String[] {"Satınalma tələbləri", "/purchasing"},
            "warehouse", new String[] {"Anbar əməliyyatları", "/warehouse"},
            "production", new String[] {"İstehsaldakı sifarişlər", "/production"},
            "completion", new String[] {"Təhvil gözləyir", "/completion"});

    private final Settings settings = new Settings();
    private final Map<String, Warehouse> warehouses = new LinkedHashMap<>();
    private final Map<String, Material> materials = new LinkedHashMap<>();
    private final Map<Integer, Workshop> workshops = new LinkedHashMap<>();
    private final Map<String, Recipe> recipes = new LinkedHashMap<>();
    private final Map<String, Order> orders = new ConcurrentSkipListMap<>();
    private final Map<String, PurchaseRequest> purchases = new ConcurrentSkipListMap<>();
    private int orderSeq;
    private int purchaseSeq;

    public void addWarehouse(Warehouse warehouse) {
        warehouses.put(warehouse.name(), warehouse);
    }

    public void addMaterial(Material material) {
        materials.put(material.getCode(), material);
    }

    public void addWorkshop(Workshop workshop) {
        workshops.put(workshop.no(), workshop);
    }

    public void addRecipe(Recipe recipe) {
        recipes.put(recipe.product().getCode(), recipe);
    }

    public Collection<Warehouse> warehouses() {
        return warehouses.values();
    }

    public Warehouse warehouse(String name) {
        return warehouses.get(name);
    }

    public Collection<Material> materials() {
        return materials.values();
    }

    public List<Material> materials(Warehouse warehouse) {
        return materials.values().stream().filter(m -> m.getWarehouse().equals(warehouse)).toList();
    }

    /** What can go on a design's material list: everything except finished products. */
    public List<Material> components() {
        return materials.values().stream().filter(m -> m.getType() != MaterialType.HAZIR_MEHSUL).toList();
    }

    public Material material(String code) {
        Material material = materials.get(code);
        if (material == null) {
            throw new IllegalArgumentException("Material tapılmadı: " + code);
        }
        return material;
    }

    public Collection<Workshop> workshops() {
        return workshops.values();
    }

    public List<ProcessRef> processes() {
        return workshops.values().stream().flatMap(w -> w.processes().stream()).toList();
    }

    public Collection<Recipe> recipes() {
        return recipes.values();
    }

    public List<Recipe> productRecipes() {
        return recipes.values().stream().filter(r -> r.product().getType() == MaterialType.HAZIR_MEHSUL).toList();
    }

    public Recipe recipe(String code) {
        Recipe recipe = recipes.get(code);
        if (recipe == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return recipe;
    }

    public boolean hasRecipe(String code) {
        return recipes.containsKey(code);
    }

    public Order order(String no) {
        Order order = orders.get(no);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return order;
    }

    public List<Order> orders(OrderStatus status) {
        return filter(o -> status == null || o.getStatus() == status);
    }

    public long count(OrderStatus status) {
        return orders(status).size();
    }

    public List<Order> designQueue() {
        return orders(OrderStatus.DIZAYN);
    }

    public List<Order> costingQueue() {
        return filter(o -> o.getStatus() == OrderStatus.KALKULYASIYA && !o.isCosted());
    }

    public List<Order> approvalQueue() {
        return filter(o -> o.getStatus() == OrderStatus.KALKULYASIYA && o.isCosted());
    }

    public List<Order> supplyQueue() {
        return orders(OrderStatus.TESDIQLENDI);
    }

    public List<Order> productionQueue() {
        return filter(o -> o.getStatus() == OrderStatus.ISTEHSALDA && o.getProducedAt() == null);
    }

    public List<Order> deliveryQueue() {
        return filter(o -> o.getStatus() == OrderStatus.ISTEHSALDA && o.getProducedAt() != null);
    }

    public List<Order> issueQueue() {
        return filter(o -> o.getStatus() == OrderStatus.XAMMAL
                && o.getRequirements().stream().anyMatch(r -> r.getReserved().signum() > 0));
    }

    public List<Order> supplyInProgress() {
        return orders(OrderStatus.XAMMAL);
    }

    public Collection<PurchaseRequest> purchases() {
        return purchases.values();
    }

    public List<PurchaseRequest> purchases(PurchaseStatus status) {
        return purchases.values().stream().filter(p -> p.getStatus() == status).toList();
    }

    public List<PurchaseRequest> purchases(Order order) {
        return purchases.values().stream().filter(p -> p.getOrder() == order).toList();
    }

    /** The work waiting for a role, one line per stage it handles. */
    public List<Task> tasks(Role role) {
        List<Task> tasks = new ArrayList<>();
        counts().forEach((stage, count) -> {
            if (role.sees(stage)) {
                String[] target = TASK_TARGETS.get(stage);
                tasks.add(new Task(target[0], count, target[1]));
            }
        });
        return tasks;
    }

    public Settings settings() {
        return settings;
    }

    public synchronized void saveSettings(String companyName, BigDecimal expensePct, BigDecimal profitPct, Integer productionDays) {
        if (isBlank(companyName)) {
            throw new IllegalArgumentException("Şirkətin adı mütləqdir.");
        }
        if (expensePct == null || expensePct.signum() < 0 || profitPct == null || profitPct.signum() < 0) {
            throw new IllegalArgumentException("Xərc və gəlir faizi mənfi ola bilməz.");
        }
        if (productionDays == null || productionDays < 1) {
            throw new IllegalArgumentException("İstehsal müddəti ən azı 1 gün olmalıdır.");
        }
        settings.setCompanyName(companyName.trim());
        settings.setExpensePct(expensePct);
        settings.setProfitPct(profitPct);
        settings.setProductionDays(productionDays);
    }

    public Map<String, Integer> counts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("orders", orders(OrderStatus.YENI).size());
        counts.put("design", designQueue().size());
        counts.put("costing", costingQueue().size());
        counts.put("approval", approvalQueue().size());
        counts.put("supply", supplyQueue().size());
        counts.put("purchasing", purchases(PurchaseStatus.TELEB).size());
        counts.put("warehouse", issueQueue().size() + purchases(PurchaseStatus.SIFARIS).size());
        counts.put("production", productionQueue().size());
        counts.put("completion", deliveryQueue().size());
        return counts;
    }

    // 1-2. Sifarişin qəbulu və növü

    public synchronized Order createOrder(OrderForm form) {
        if (isBlank(form.customer()) || isBlank(form.manager()) || form.type() == null) {
            throw new IllegalArgumentException("Müştəri, menecer və sifariş növü mütləqdir.");
        }
        if (form.qty() == null || form.qty() < 1) {
            throw new IllegalArgumentException("Sifariş miqdarı ən azı 1 olmalıdır.");
        }
        Order order = new Order("SF-2026-%04d".formatted(++orderSeq), form.type());
        order.setCustomer(form.customer().trim());
        order.setPhone(form.phone());
        order.setManager(form.manager().trim());
        order.setSpecialCustomer(Boolean.TRUE.equals(form.specialCustomer()));
        order.setQty(form.qty());
        order.setRequestedDate(form.requestedDate());
        order.setNote(form.note());
        order.setExpensePct(settings.getExpensePct());
        order.setProfitPct(settings.getProfitPct());
        if (form.type().isDesignRequired()) {
            if (isBlank(form.product())) {
                throw new IllegalArgumentException("Məhsulun adını daxil edin.");
            }
            order.setProduct(form.product().trim());
        } else {
            if (isBlank(form.recipeCode())) {
                throw new IllegalArgumentException("Seri sifariş üçün resept seçin.");
            }
            Recipe recipe = recipe(form.recipeCode());
            order.setRecipe(recipe);
            order.setProduct(recipe.product().getName());
            order.setLines(new ArrayList<>(recipe.lines().stream()
                    .map(l -> new OrderLine(l.material(), l.qty())).toList()));
            order.setRoute(new ArrayList<>(recipe.route().stream().map(RouteStep::new).toList()));
        }
        orders.put(order.getNo(), order);
        return order;
    }

    public synchronized void routeOrder(String no) {
        Order order = order(no);
        require(order, OrderStatus.YENI);
        order.setStatus(order.getType().isDesignRequired() ? OrderStatus.DIZAYN : OrderStatus.KALKULYASIYA);
    }

    // 3. Dizayn

    public synchronized void saveDesign(String no, String designer, String cadFile, String dimensions, String notes,
                                        List<String> materialCodes, List<BigDecimal> quantities, List<String> routeIds) {
        Order order = order(no);
        require(order, OrderStatus.DIZAYN);
        if (isBlank(cadFile)) {
            throw new IllegalArgumentException("AutoCAD faylını qeyd edin.");
        }
        List<OrderLine> lines = new ArrayList<>();
        if (new HashSet<>(materialCodes).size() != materialCodes.size()) {
            throw new IllegalArgumentException("Eyni material siyahıda iki dəfə ola bilməz.");
        }
        for (int i = 0; i < materialCodes.size(); i++) {
            BigDecimal qty = quantities.get(i);
            if (qty == null || qty.signum() <= 0) {
                throw new IllegalArgumentException("Material miqdarı müsbət olmalıdır.");
            }
            lines.add(new OrderLine(material(materialCodes.get(i)), qty));
        }
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Ən azı bir material əlavə edin.");
        }
        if (routeIds.isEmpty()) {
            throw new IllegalArgumentException("Ən azı bir istehsal prosesi seçin.");
        }
        order.setDesigner(designer);
        order.setCadFile(cadFile);
        order.setDimensions(dimensions);
        order.setTechnicalNotes(notes);
        order.setLines(lines);
        order.setRoute(new ArrayList<>(processes().stream()
                .filter(p -> routeIds.contains(p.id())).map(RouteStep::new).toList()));
        order.setStatus(OrderStatus.KALKULYASIYA);
    }

    // 4. Kalkulyasiya və planlama

    public synchronized void saveCosting(String no, Map<String, BigDecimal> manualPrices, BigDecimal expensePct,
                                         BigDecimal profitPct, Integer productionDays, LocalDate productionDate,
                                         String conditions) {
        Order order = order(no);
        require(order, OrderStatus.KALKULYASIYA);
        if (order.isCosted()) {
            throw new IllegalStateException("Kalkulyasiya artıq təsdiqə göndərilib.");
        }
        if (expensePct == null || expensePct.signum() < 0 || profitPct == null || profitPct.signum() < 0) {
            throw new IllegalArgumentException("Xərc və gəlir faizi mənfi ola bilməz.");
        }
        if (productionDays == null || productionDays < 1 || productionDate == null) {
            throw new IllegalArgumentException("İstehsal tarixi və müddəti mütləqdir.");
        }
        for (OrderLine line : order.getLines()) {
            if (line.getMaterial().getLastPrice() == null) {
                BigDecimal manual = manualPrices.get(line.getMaterial().getCode());
                if (manual == null || manual.signum() <= 0) {
                    throw new IllegalArgumentException(
                            "Son alış qiyməti olmayan material üçün qiymət daxil edin: " + line.getMaterial().getName());
                }
                line.setManualPrice(manual);
            }
        }
        order.setExpensePct(expensePct);
        order.setProfitPct(profitPct);
        order.setProductionDays(productionDays);
        order.setProductionDate(productionDate);
        order.setConditions(conditions);
        order.setCosted(true);
    }

    // 5. Müştəri və menecer təsdiqi

    public synchronized void decide(String no, boolean approved, String reason) {
        Order order = order(no);
        require(order, OrderStatus.KALKULYASIYA);
        if (!order.isCosted()) {
            throw new IllegalStateException("Əvvəlcə kalkulyasiya tamamlanmalıdır.");
        }
        if (approved) {
            order.setStatus(OrderStatus.TESDIQLENDI);
            return;
        }
        if (isBlank(reason)) {
            throw new IllegalArgumentException("İmtina səbəbini qeyd edin.");
        }
        order.setStopReason(reason.trim());
        order.setStatus(OrderStatus.DAYANDIRILDI);
    }

    // 6. Xammal təminatı

    /** Stock check for an order, not saved. Reservations made for other orders are not counted as available. */
    public List<Requirement> previewRequirements(Order order) {
        return order.getLines().stream().map(line -> {
            BigDecimal required = line.getQtyPerUnit().multiply(BigDecimal.valueOf(order.getQty()));
            BigDecimal available = line.getMaterial().getAvailable().max(BigDecimal.ZERO);
            return new Requirement(line.getMaterial(), required, required.min(available));
        }).toList();
    }

    public synchronized void openProductionOrder(String no) {
        Order order = order(no);
        require(order, OrderStatus.TESDIQLENDI);
        List<Requirement> requirements = previewRequirements(order);
        for (Requirement requirement : requirements) {
            Material material = requirement.getMaterial();
            material.setReserved(material.getReserved().add(requirement.getFromStock()));
            if (requirement.getShortage().signum() > 0) {
                String prNo = "SA-2026-%04d".formatted(++purchaseSeq);
                purchases.put(prNo, new PurchaseRequest(prNo, order, requirement, requirement.getShortage()));
            }
        }
        order.setRequirements(requirements);
        order.setStatus(OrderStatus.XAMMAL);
    }

    // 7. Satınalma

    public synchronized void placePurchase(String no, String supplier, LocalDate expectedDate) {
        PurchaseRequest purchase = purchase(no);
        if (purchase.getStatus() != PurchaseStatus.TELEB) {
            throw new IllegalStateException("Bu tələb üzrə sifariş artıq verilib.");
        }
        if (isBlank(supplier)) {
            throw new IllegalArgumentException("Təchizatçını qeyd edin.");
        }
        purchase.setSupplier(supplier.trim());
        purchase.setExpectedDate(expectedDate);
        purchase.setStatus(PurchaseStatus.SIFARIS);
    }

    // 8. Anbar

    public synchronized void receivePurchase(String no, String invoiceNo) {
        PurchaseRequest purchase = purchase(no);
        if (purchase.getStatus() != PurchaseStatus.SIFARIS) {
            throw new IllegalStateException("Yalnız sifariş verilmiş mal qəbul edilə bilər.");
        }
        if (isBlank(invoiceNo)) {
            throw new IllegalArgumentException("Qaimə nömrəsini daxil edin.");
        }
        Material material = purchase.getMaterial();
        Requirement requirement = purchase.getRequirement();
        material.setStock(material.getStock().add(purchase.getQty()));
        material.setReserved(material.getReserved().add(purchase.getQty()));
        requirement.setReserved(requirement.getReserved().add(purchase.getQty()));
        purchase.setInvoiceNo(invoiceNo.trim());
        purchase.setReceivedAt(LocalDate.now());
        purchase.setStatus(PurchaseStatus.QEBUL);
    }

    /** Hands over everything held for the order; it enters production once the last material has been issued. */
    public synchronized void issueMaterials(String no) {
        Order order = order(no);
        require(order, OrderStatus.XAMMAL);
        for (Requirement requirement : order.getRequirements()) {
            Material material = requirement.getMaterial();
            BigDecimal held = requirement.getReserved();
            material.setStock(material.getStock().subtract(held));
            material.setReserved(material.getReserved().subtract(held));
            requirement.setIssued(requirement.getIssued().add(held));
            requirement.setReserved(BigDecimal.ZERO);
        }
        if (order.getRequirements().stream().allMatch(Requirement::isFullyIssued)) {
            order.setStatus(OrderStatus.ISTEHSALDA);
        }
    }

    // 9. İstehsal

    public synchronized void startStep(String no, int index) {
        Order order = order(no);
        require(order, OrderStatus.ISTEHSALDA);
        RouteStep step = order.getRoute().get(index);
        if (step.getStartedAt() == null) {
            step.setStartedAt(LocalDate.now());
        }
    }

    public synchronized void finishStep(String no, int index) {
        Order order = order(no);
        require(order, OrderStatus.ISTEHSALDA);
        RouteStep step = order.getRoute().get(index);
        if (step.getStartedAt() == null) {
            throw new IllegalStateException("Proses əvvəlcə başladılmalıdır.");
        }
        step.setFinishedAt(LocalDate.now());
    }

    public synchronized void completeProduction(String no, String warehouseName) {
        Order order = order(no);
        require(order, OrderStatus.ISTEHSALDA);
        if (!order.isRouteDone()) {
            throw new IllegalStateException("Bütün proseslər tamamlanmalıdır.");
        }
        if (warehouse(warehouseName) == null) {
            throw new IllegalArgumentException("Hazır məhsul anbarını seçin.");
        }
        order.setFinishedWarehouse(warehouseName);
        order.setProducedAt(LocalDate.now());
        if (order.getRecipe() != null) {
            Material product = order.getRecipe().product();
            product.setStock(product.getStock().add(BigDecimal.valueOf(order.getQty())));
        }
    }

    // 10. Tamamlanma

    public synchronized void deliver(String no) {
        Order order = order(no);
        require(order, OrderStatus.ISTEHSALDA);
        if (order.getProducedAt() == null) {
            throw new IllegalStateException("Məhsul hələ istehsal edilməyib.");
        }
        if (order.getRecipe() != null) {
            Material product = order.getRecipe().product();
            product.setStock(product.getStock().subtract(BigDecimal.valueOf(order.getQty())));
        }
        order.setDeliveredAt(LocalDate.now());
        order.setStatus(OrderStatus.TAMAMLANDI);
    }

    private PurchaseRequest purchase(String no) {
        PurchaseRequest purchase = purchases.get(no);
        if (purchase == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return purchase;
    }

    private List<Order> filter(Predicate<Order> predicate) {
        return orders.values().stream().filter(predicate).toList().reversed();
    }

    private static void require(Order order, OrderStatus status) {
        if (order.getStatus() != status) {
            throw new IllegalStateException(
                    "%s bu mərhələdə deyil (status: %s).".formatted(order.getNo(), order.getStatus().getLabel()));
        }
    }

    private static boolean isBlank(String value) {
        return Objects.toString(value, "").isBlank();
    }
}
