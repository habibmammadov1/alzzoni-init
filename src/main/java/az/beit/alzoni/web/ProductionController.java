package az.beit.alzoni.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import az.beit.alzoni.domain.Order;
import az.beit.alzoni.domain.OrderStatus;
import az.beit.alzoni.service.ErpService;

@Controller
public class ProductionController {

    private final ErpService erp;

    public ProductionController(ErpService erp) {
        this.erp = erp;
    }

    @GetMapping("/production")
    public String queue(Model model) {
        return Queue.show(model, 9, "İstehsal", "Sexlərə ötürülmüş sifarişlər. Proseslər resept və dizayn üzrə qeyd olunur.",
                erp.productionQueue(), "/production/", "Prosesləri aç");
    }

    @GetMapping("/production/{no}")
    public String detail(@PathVariable String no, Model model) {
        Order order = erp.order(no);
        if (order.getStatus() != OrderStatus.ISTEHSALDA || order.getProducedAt() != null) {
            return "redirect:/orders/" + no;
        }
        model.addAttribute("order", order);
        model.addAttribute("warehouses", erp.warehouses());
        return "production/detail";
    }

    @PostMapping("/production/{no}/steps/{index}/start")
    public String start(@PathVariable String no, @PathVariable int index) {
        erp.startStep(no, index);
        return "redirect:/production/" + no;
    }

    @PostMapping("/production/{no}/steps/{index}/finish")
    public String finish(@PathVariable String no, @PathVariable int index) {
        erp.finishStep(no, index);
        return "redirect:/production/" + no;
    }

    @PostMapping("/production/{no}/complete")
    public String complete(@PathVariable String no, @RequestParam(required = false) String warehouse,
                           RedirectAttributes redirect) {
        erp.completeProduction(no, warehouse);
        redirect.addFlashAttribute("success", "Məhsul istehsal edildi: " + no);
        return "redirect:/completion";
    }

    @GetMapping("/completion")
    public String completion(Model model) {
        model.addAttribute("ready", erp.deliveryQueue());
        model.addAttribute("done", erp.orders(OrderStatus.TAMAMLANDI));
        return "completion/list";
    }

    @PostMapping("/completion/{no}/deliver")
    public String deliver(@PathVariable String no, RedirectAttributes redirect) {
        erp.deliver(no);
        redirect.addFlashAttribute("success", no + " müştəriyə təhvil verildi və tamamlandı.");
        return "redirect:/completion";
    }
}
