package az.beit.alzoni.domain;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

/** What an order needs of one material once its production order is opened. */
@Getter
@Setter
public class Requirement {
    private final Material material;
    private final BigDecimal required;
    private final BigDecimal fromStock;
    private final BigDecimal shortage;
    /** Held in the warehouse for this order, waiting to be handed over. */
    private BigDecimal reserved;
    private BigDecimal issued = BigDecimal.ZERO;

    public Requirement(Material material, BigDecimal required, BigDecimal fromStock) {
        this.material = material;
        this.required = required;
        this.fromStock = fromStock;
        this.shortage = required.subtract(fromStock);
        this.reserved = fromStock;
    }

    public boolean isFullyIssued() {
        return issued.compareTo(required) >= 0;
    }

    public String getResult() {
        if (shortage.signum() == 0) {
            return "Tam var";
        }
        return fromStock.signum() == 0 ? "Heç yoxdur" : "Qismən var";
    }

    public String getResultColor() {
        if (shortage.signum() == 0) {
            return "green";
        }
        return fromStock.signum() == 0 ? "red" : "yellow";
    }
}
