package az.beit.alzoni.domain;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

/** One material on an order, per single unit of product. */
@Getter
@Setter
public class OrderLine {
    private final Material material;
    private final BigDecimal qtyPerUnit;
    private BigDecimal manualPrice;

    public OrderLine(Material material, BigDecimal qtyPerUnit) {
        this.material = material;
        this.qtyPerUnit = qtyPerUnit;
    }

    public boolean isPriced() {
        return getUnitPrice() != null;
    }

    public BigDecimal getUnitPrice() {
        return material.getLastPrice() != null ? material.getLastPrice() : manualPrice;
    }

    public BigDecimal getCost() {
        return isPriced() ? getUnitPrice().multiply(qtyPerUnit) : BigDecimal.ZERO;
    }
}
