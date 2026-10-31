package az.beit.alzoni.web;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import az.beit.alzoni.service.ErpService;

@Controller
@RequestMapping("/purchasing")
public class PurchasingController {

    private final ErpService erp;

    public PurchasingController(ErpService erp) {
        this.erp = erp;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("purchases", erp.purchases());
        return "purchasing/list";
    }

    @PostMapping("/{no}/order")
    public String order(@PathVariable String no, @RequestParam(required = false) String supplier,
                        @RequestParam(required = false) LocalDate expectedDate, RedirectAttributes redirect) {
        erp.placePurchase(no, supplier, expectedDate);
        redirect.addFlashAttribute("success", no + " üzrə alış sifarişi verildi.");
        return "redirect:/purchasing";
    }
}
