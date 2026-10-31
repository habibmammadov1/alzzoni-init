package az.beit.alzoni.web;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Component;

/** Display formatting for the templates, used as {@code @fmt}. */
@Component("fmt")
public class Fmt {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public String money(BigDecimal value) {
        return value == null ? "—" : new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US)).format(value) + " AZN";
    }

    public String qty(BigDecimal value) {
        return value == null ? "—" : value.stripTrailingZeros().toPlainString();
    }

    public String date(LocalDate value) {
        return value == null ? "—" : DATE.format(value);
    }

    public String text(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }
}
