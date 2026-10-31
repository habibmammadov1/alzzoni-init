package az.beit.alzoni.domain;

import lombok.Getter;

@Getter
public enum MaterialType {
    XAMMAL("Xammal"),
    YARIMFABRIKAT("Yarımfabrikat"),
    HAZIR_MEHSUL("Hazır məhsul");

    private final String label;

    MaterialType(String label) {
        this.label = label;
    }
}
