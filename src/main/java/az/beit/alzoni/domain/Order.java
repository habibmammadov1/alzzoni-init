package az.beit.alzoni.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Order {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final String no;
    private final OrderType type;
    private final LocalDate createdAt = LocalDate.now();
    private String customer;
    private String phone;
    private String manager;
    private boolean specialCustomer;
    private String product;
    private Recipe recipe;
    private int qty;
    private LocalDate requestedDate;
    private String note;
    private OrderStatus status = OrderStatus.YENI;

    private String designer;
    private String cadFile;
    private String dimensions;
    private String technicalNotes;

    private List<OrderLine> lines = new ArrayList<>();
    private List<RouteStep> route = new ArrayList<>();

    private boolean costed;
    private BigDecimal expensePct = BigDecimal.valueOf(25);
    private BigDecimal profitPct = BigDecimal.valueOf(50);
    private int productionDays;
    private LocalDate productionDate;
    private String conditions;

    private String stopReason;
    private List<Requirement> requirements = new ArrayList<>();
    private LocalDate producedAt;
    private String finishedWarehouse;
    private LocalDate deliveredAt;

    public Order(String no, OrderType type) {
        this.no = no;
        this.type = type;
    }

    public BigDecimal getMaterialCost() {
        return lines.stream().map(OrderLine::getCost).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getExpense() {
        return percent(getMaterialCost(), expensePct);
    }

    public BigDecimal getTotalCost() {
        return getMaterialCost().add(getExpense());
    }

    public BigDecimal getProfit() {
        return percent(getTotalCost(), profitPct);
    }

    public BigDecimal getUnitPrice() {
        return getTotalCost().add(getProfit());
    }

    public BigDecimal getOrderTotal() {
        return getUnitPrice().multiply(BigDecimal.valueOf(qty));
    }

    public LocalDate getReadyDate() {
        return productionDate == null ? null : productionDate.plusDays(productionDays);
    }

    public boolean isRouteDone() {
        return !route.isEmpty() && route.stream().allMatch(RouteStep::isDone);
    }

    /** Tells the status stepper how to draw {@code step} for this order. */
    public String stepState(OrderStatus step) {
        if (status == OrderStatus.DAYANDIRILDI) {
            return step.ordinal() <= OrderStatus.KALKULYASIYA.ordinal() ? "done" : "todo";
        }
        if (step == OrderStatus.DIZAYN && !type.isDesignRequired()) {
            return "skipped";
        }
        if (step == status) {
            return status == OrderStatus.TAMAMLANDI ? "done" : "current";
        }
        return step.ordinal() < status.ordinal() ? "done" : "todo";
    }

    private static BigDecimal percent(BigDecimal base, BigDecimal pct) {
        return base.multiply(pct).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }
}
