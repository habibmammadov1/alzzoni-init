package az.beit.alzoni.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import az.beit.alzoni.domain.Order;
import az.beit.alzoni.domain.OrderStatus;
import az.beit.alzoni.domain.OrderType;
import az.beit.alzoni.domain.PurchaseStatus;
import az.beit.alzoni.service.ErpService;
import az.beit.alzoni.service.OrderForm;

@Controller
public class OrderController {

    private final ErpService erp;

    public OrderController(ErpService erp) {
        this.erp = erp;
    }

    @GetMapping("/")
    public String flow(Model model) {
        model.addAttribute("erp", erp);
        model.addAttribute("recent", erp.orders(null).stream().limit(5).toList());
        model.addAttribute("openPurchases", erp.purchases(PurchaseStatus.TELEB).size() + erp.purchases(PurchaseStatus.SIFARIS).size());
        return "index";
    }

    @GetMapping("/orders")
    public String list(@RequestParam(required = false) OrderStatus status, Model model) {
        model.addAttribute("orders", erp.orders(status));
        model.addAttribute("selected", status);
        model.addAttribute("statuses", OrderStatus.values());
        return "orders/list";
    }

    @GetMapping("/orders/new")
    public String newOrder(Model model) {
        model.addAttribute("types", OrderType.values());
        model.addAttribute("recipes", erp.productRecipes());
        return "orders/new";
    }

    @PostMapping("/orders")
    public String create(@ModelAttribute OrderForm form) {
        return "redirect:/orders/" + erp.createOrder(form).getNo();
    }

    @GetMapping("/orders/{no}")
    public String detail(@PathVariable String no, Model model) {
        Order order = erp.order(no);
        model.addAttribute("order", order);
        model.addAttribute("purchases", erp.purchases(order));
        return "orders/detail";
    }

    @PostMapping("/orders/{no}/route")
    public String route(@PathVariable String no) {
        erp.routeOrder(no);
        return "redirect:/orders/" + no;
    }
}
