package az.beit.alzoni.domain;

import java.math.BigDecimal;
import java.util.List;

public record Recipe(Material product, String techCard, List<Line> lines, List<ProcessRef> route) {

    public record Line(Material material, BigDecimal qty) {
    }
}
