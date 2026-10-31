package az.beit.alzoni.domain;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

/** Company-wide defaults; a new order starts from these and costing can still change them per order. */
@Getter
@Setter
public class Settings {
    private String companyName = "Alzoni MMC";
    private BigDecimal expensePct = BigDecimal.valueOf(25);
    private BigDecimal profitPct = BigDecimal.valueOf(50);
    private int productionDays = 10;
}
