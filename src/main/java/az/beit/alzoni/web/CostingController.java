package az.beit.alzoni.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import az.beit.alzoni.domain.Order;
import az.beit.alzoni.domain.OrderStatus;
import az.beit.alzoni.service.ErpService;

@Controller
@RequestMapping("/costing")
public class CostingController {

    private static final String PRICE_FIELD = "price_";

    private final ErpService erp;

    public CostingController(ErpService erp) {
        this.erp = erp;
    }

    @GetMapping
    public String queue(Model model) {
        return Queue.show(model, 4, "Kalkulyasiya və planlama",
                "Xammal maya dəyəri, əlavə xərc, gəlir və istehsal tarixi burada müəyyən edilir.",
                erp.costingQueue(), "/costing/", "Hesabla");
    }

    @GetMapping("/{no}")
    public String form(@PathVariable String no, Model model) {
        Order order = erp.order(no);
        if (order.getStatus() != OrderStatus.KALKULYASIYA || order.isCosted()) {
            return "redirect:/orders/" + no;
        }
        model.addAttribute("order", order);
        model.addAttribute("defaultDate", LocalDate.now().plusDays(1));
        return "costing/form";
    }

    @PostMapping("/{no}")
    public String save(@PathVariable String no, @RequestParam BigDecimal expensePct, @RequestParam BigDecimal profitPct,
                       @RequestParam(required = false) Integer productionDays,
                       @RequestParam(required = false) LocalDate productionDate,
                       @RequestParam(required = false) String conditions,
                       @RequestParam Map<String, String> params, RedirectAttributes redirect) {
        Map<String, BigDecimal> manualPrices = new HashMap<>();
        params.forEach((name, value) -> {
            if (name.startsWith(PRICE_FIELD) && !value.isBlank()) {
                manualPrices.put(name.substring(PRICE_FIELD.length()), new BigDecimal(value));
            }
        });
        erp.saveCosting(no, manualPrices, expensePct, profitPct, productionDays, productionDate, conditions);
        redirect.addFlashAttribute("success", "Kalkulyasiya hazırdır. Sifariş menecerin təsdiqinə qaytarıldı.");
        return "redirect:/approval/" + no;
    }
}
