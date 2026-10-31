package az.beit.alzoni.web;

import java.math.BigDecimal;
import java.util.List;

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
@RequestMapping("/design")
public class DesignController {

    private final ErpService erp;

    public DesignController(ErpService erp) {
        this.erp = erp;
    }

    @GetMapping
    public String queue(Model model) {
        return Queue.show(model, 3, "Dizayn", "Yeni və özəl sifarişlər dizaynerə gəlir. Seri sifarişlərdə bu mərhələ olmur.",
                erp.designQueue(), "/design/", "Dizayn et");
    }

    @GetMapping("/{no}")
    public String form(@PathVariable String no, Model model) {
        Order order = erp.order(no);
        if (order.getStatus() != OrderStatus.DIZAYN) {
            return "redirect:/orders/" + no;
        }
        model.addAttribute("order", order);
        model.addAttribute("materials", erp.components());
        model.addAttribute("workshops", erp.workshops());
        return "design/form";
    }

    @PostMapping("/{no}")
    public String save(@PathVariable String no, @RequestParam(required = false) String designer,
                       @RequestParam(required = false) String cadFile, @RequestParam(required = false) String dimensions,
                       @RequestParam(required = false) String technicalNotes,
                       @RequestParam(defaultValue = "") List<String> materialCode,
                       @RequestParam(defaultValue = "") List<BigDecimal> materialQty,
                       @RequestParam(defaultValue = "") List<String> route, RedirectAttributes redirect) {
        erp.saveDesign(no, designer, cadFile, dimensions, technicalNotes, materialCode, materialQty, route);
        redirect.addFlashAttribute("success", no + " planlama mərhələsinə ötürüldü.");
        return "redirect:/costing/" + no;
    }
}
