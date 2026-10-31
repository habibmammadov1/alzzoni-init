package az.beit.alzoni.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseRequest {
    private final String no;
    private final Order order;
    private final Requirement requirement;
    private final BigDecimal qty;
    private PurchaseStatus status = PurchaseStatus.TELEB;
    private String supplier;
    private LocalDate expectedDate;
    private String invoiceNo;
    private LocalDate receivedAt;

    public PurchaseRequest(String no, Order order, Requirement requirement, BigDecimal qty) {
        this.no = no;
        this.order = order;
        this.requirement = requirement;
        this.qty = qty;
    }

    public Material getMaterial() {
        return requirement.getMaterial();
    }
}
