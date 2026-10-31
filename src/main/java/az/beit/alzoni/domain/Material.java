package az.beit.alzoni.domain;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Material {
    private final String code;
    private final String name;
    private final MaterialType type;
    private final String unit;
    private final Warehouse warehouse;
    /** Null when the material has never been bought; the price is then typed in during costing. */
    private BigDecimal lastPrice;
    private BigDecimal stock;
    private BigDecimal reserved = BigDecimal.ZERO;

    public Material(String code, String name, MaterialType type, String unit, Warehouse warehouse,
                    BigDecimal lastPrice, BigDecimal stock) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.unit = unit;
        this.warehouse = warehouse;
        this.lastPrice = lastPrice;
        this.stock = stock;
    }

    public BigDecimal getAvailable() {
        return stock.subtract(reserved);
    }
}
