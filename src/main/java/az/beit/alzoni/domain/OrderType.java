package az.beit.alzoni.domain;

import lombok.Getter;

@Getter
public enum OrderType {
    YENI("Yeni sifariş", "Yeni məhsul, 1 dəfəlik dizayn", true),
    OZEL("Özəl sifariş", "Müştəriyə xüsusi məhsul, smeta aparılır", true),
    SERI("Seri sifariş", "Mövcud məhsul, resept və texnoloji kart üzrə", false);

    private final String label;
    private final String hint;
    private final boolean designRequired;

    OrderType(String label, String hint, boolean designRequired) {
        this.label = label;
        this.hint = hint;
        this.designRequired = designRequired;
    }
}
