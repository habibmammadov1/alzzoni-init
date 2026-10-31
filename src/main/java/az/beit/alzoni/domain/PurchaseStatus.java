package az.beit.alzoni.domain;

import lombok.Getter;

@Getter
public enum PurchaseStatus {
    TELEB("Tələb daxil oldu", "yellow"),
    SIFARIS("Təchizatçıya sifariş verildi", "blue"),
    QEBUL("Anbara mədaxil edildi", "green");

    private final String label;
    private final String color;

    PurchaseStatus(String label, String color) {
        this.label = label;
        this.color = color;
    }
}
