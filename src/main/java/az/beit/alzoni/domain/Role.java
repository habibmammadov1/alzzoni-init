package az.beit.alzoni.domain;

import java.util.List;

import lombok.Getter;

/** Who works where in the flow. {@code stages} are the task keys of ErpService; empty means every stage. */
@Getter
public enum Role {
    ADMIN("Administrator"),
    DIREKTOR("Direktor"),
    SATIS("Satış meneceri", "orders", "approval", "completion"),
    DIZAYNER("Dizayner", "design"),
    PLANLAMA("Planlama / Kalkulyasiya", "costing", "supply"),
    SATINALMA("Satınalma meneceri", "purchasing"),
    ANBAR("Anbardar", "warehouse"),
    ISTEHSAL("İstehsalçı", "production");

    private final String label;
    private final List<String> stages;

    Role(String label, String... stages) {
        this.label = label;
        this.stages = List.of(stages);
    }

    public boolean sees(String stage) {
        return stages.isEmpty() || stages.contains(stage);
    }
}
