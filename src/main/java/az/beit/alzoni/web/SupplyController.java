package az.beit.alzoni.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import az.beit.alzoni.domain.Order;
import az.beit.alzoni.domain.OrderStatus;
import az.beit.alzoni.service.ErpService;

@Controller
@RequestMapping("/supply")
public class SupplyController {

    private final ErpService erp;

    public SupplyController(ErpService erp) {
        this.erp = erp;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("queue", erp.supplyQueue());
        model.addAttribute("inProgress", erp.supplyInProgress());
        return "supply/list";
    }

    @GetMapping("/{no}")
    public String check(@PathVariable String no, Model model) {
        Order order = erp.order(no);
        if (order.getStatus() != OrderStatus.TESDIQLENDI) {
            return "redirect:/orders/" + no;
        }
        model.addAttribute("order", order);
        model.addAttribute("requirements", erp.previewRequirements(order));
        return "supply/check";
    }

    @PostMapping("/{no}/open")
    public String open(@PathVariable String no, RedirectAttributes redirect) {
        erp.openProductionOrder(no);
        redirect.addFlashAttribute("success", "İstehsal sifarişi açıldı. Çatışan miqdar satınalmaya göndərildi.");
        return "redirect:/orders/" + no;
    }
}
