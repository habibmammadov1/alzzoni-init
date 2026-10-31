package az.beit.alzoni.web;

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
@RequestMapping("/approval")
public class ApprovalController {

    private final ErpService erp;

    public ApprovalController(ErpService erp) {
        this.erp = erp;
    }

    @GetMapping
    public String queue(Model model) {
        return Queue.show(model, 5, "Müştəri və menecer təsdiqi",
                "Menecer qiyməti və çatdırılma tarixini müştəriyə təqdim edir, son təsdiqi verir.",
                erp.approvalQueue(), "/approval/", "Təsdiqə bax");
    }

    @GetMapping("/{no}")
    public String form(@PathVariable String no, Model model) {
        Order order = erp.order(no);
        if (order.getStatus() != OrderStatus.KALKULYASIYA || !order.isCosted()) {
            return "redirect:/orders/" + no;
        }
        model.addAttribute("order", order);
        return "approval/form";
    }

    @PostMapping("/{no}")
    public String decide(@PathVariable String no, @RequestParam boolean approved,
                         @RequestParam(required = false) String reason, RedirectAttributes redirect) {
        erp.decide(no, approved, reason);
        redirect.addFlashAttribute("success", approved ? "Sifariş təsdiqləndi." : "Proses dayandırıldı.");
        return approved ? "redirect:/supply/" + no : "redirect:/orders/" + no;
    }
}
