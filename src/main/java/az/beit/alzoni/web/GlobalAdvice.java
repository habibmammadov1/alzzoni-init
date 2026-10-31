package az.beit.alzoni.web;

import java.net.URI;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import az.beit.alzoni.domain.OrderStatus;
import az.beit.alzoni.service.ErpService;
import az.beit.alzoni.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalAdvice {

    private final ErpService erp;
    private final UserService users;

    public GlobalAdvice(ErpService erp, UserService users) {
        this.erp = erp;
        this.users = users;
    }

    @ModelAttribute
    public void layoutData(Model model, HttpServletRequest request) {
        model.addAttribute("path", request.getRequestURI());
        model.addAttribute("me", users.current(request));
        model.addAttribute("settings", erp.settings());
        model.addAttribute("nav", erp.counts());
        model.addAttribute("flowStatuses", OrderStatus.flow());
    }

    /** Rejected form or step: back to the page it came from, with the reason shown on top. */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public String rejected(RuntimeException e, HttpServletRequest request, RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", e.getMessage());
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer == null ? "/" : URI.create(referer).getPath());
    }
}
