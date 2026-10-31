package az.beit.alzoni.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import az.beit.alzoni.domain.MaterialType;
import az.beit.alzoni.service.ErpService;

/** Master data the flow reads from: the lists named in protocol section 5. */
@Controller
public class ReferenceController {

    private final ErpService erp;

    public ReferenceController(ErpService erp) {
        this.erp = erp;
    }

    @GetMapping("/recipes")
    public String recipes(Model model) {
        model.addAttribute("recipes", erp.recipes());
        return "reference/recipes";
    }

    @GetMapping("/recipes/{code}")
    public String recipe(@PathVariable String code, Model model) {
        model.addAttribute("recipe", erp.recipe(code));
        model.addAttribute("erp", erp);
        return "reference/recipe";
    }

    @GetMapping("/materials")
    public String materials(@RequestParam(required = false) MaterialType type, Model model) {
        model.addAttribute("materials", erp.materials().stream().filter(m -> type == null || m.getType() == type).toList());
        model.addAttribute("types", MaterialType.values());
        model.addAttribute("selected", type);
        return "reference/materials";
    }

    @GetMapping("/warehouses")
    public String warehouses(Model model) {
        model.addAttribute("warehouses", erp.warehouses());
        model.addAttribute("erp", erp);
        return "reference/warehouses";
    }

    @GetMapping("/workshops")
    public String workshops(Model model) {
        model.addAttribute("workshops", erp.workshops());
        return "reference/workshops";
    }
}
