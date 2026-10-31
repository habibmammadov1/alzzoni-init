package az.beit.alzoni.domain;

import java.util.List;

import lombok.Getter;

@Getter
public enum OrderStatus {
    YENI("Yeni", "blue"),
    DIZAYN("Dizayn mərhələsində", "purple"),
    KALKULYASIYA("Kalkulyasiya / Gözləyir", "yellow"),
    TESDIQLENDI("Müştəri təsdiqlədi", "green"),
    XAMMAL("Xammal təminatı", "blue"),
    ISTEHSALDA("İstehsalda", "purple"),
    TAMAMLANDI("Tamamlandı", "green"),
    DAYANDIRILDI("Proses dayandırıldı", "red");

    private final String label;
    private final String color;

    OrderStatus(String label, String color) {
        this.label = label;
        this.color = color;
    }

    /** The seven statuses of the normal path; DAYANDIRILDI is the exit, not a step. */
    public static List<OrderStatus> flow() {
        return List.of(YENI, DIZAYN, KALKULYASIYA, TESDIQLENDI, XAMMAL, ISTEHSALDA, TAMAMLANDI);
    }
}
