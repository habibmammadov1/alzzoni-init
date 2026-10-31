package az.beit.alzoni.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import az.beit.alzoni.domain.PurchaseStatus;
import az.beit.alzoni.service.ErpService;

@Controller
@RequestMapping("/warehouse")
public class WarehouseController {

    private final ErpService erp;

    public WarehouseController(ErpService erp) {
        this.erp = erp;
    }

    @GetMapping
    public String overview(Model model) {
        model.addAttribute("incoming", erp.purchases(PurchaseStatus.SIFARIS));
        model.addAttribute("issueQueue", erp.issueQueue());
        return "warehouse/index";
    }

    @PostMapping("/receive/{no}")
    public String receive(@PathVariable String no, @RequestParam(required = false) String invoiceNo,
                          RedirectAttributes redirect) {
        erp.receivePurchase(no, invoiceNo);
        redirect.addFlashAttribute("success", "Mal anbara mədaxil edildi.");
        return "redirect:/warehouse";
    }

    @PostMapping("/issue/{no}")
    public String issue(@PathVariable String no, RedirectAttributes redirect) {
        erp.issueMaterials(no);
        redirect.addFlashAttribute("success", no + " üçün materiallar istehsala təhvil verildi.");
        return "redirect:/warehouse";
    }
}
