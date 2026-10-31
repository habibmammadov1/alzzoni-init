package az.beit.alzoni.web;

import java.math.BigDecimal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import az.beit.alzoni.domain.Role;
import az.beit.alzoni.domain.Task;
import az.beit.alzoni.domain.User;
import az.beit.alzoni.service.ErpService;
import az.beit.alzoni.service.UserForm;
import az.beit.alzoni.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AccountController {

    private final ErpService erp;
    private final UserService users;

    public AccountController(ErpService erp, UserService users) {
        this.erp = erp;
        this.users = users;
    }

    @GetMapping("/profile")
    public String profile(HttpServletRequest request, Model model) {
        var tasks = erp.tasks(users.current(request).getRole());
        model.addAttribute("tasks", tasks);
        model.addAttribute("openTasks", tasks.stream().mapToInt(Task::count).sum());
        model.addAttribute("activeUsers", users.activeUsers());
        return "account/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam(required = false) String name, @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String email, HttpServletRequest request,
                                RedirectAttributes redirect) {
        users.updateContact(users.current(request), name, phone, email);
        redirect.addFlashAttribute("success", "Profil yeniləndi.");
        return "redirect:/profile";
    }

    @PostMapping("/profile/switch")
    public String switchUser(@RequestParam int userId, HttpServletRequest request) {
        users.switchTo(userId, request);
        return "redirect:/profile";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", users.users());
        return "account/users";
    }

    @GetMapping("/users/new")
    public String newUser(Model model) {
        model.addAttribute("roles", Role.values());
        return "account/user-form";
    }

    @PostMapping("/users")
    public String create(@ModelAttribute UserForm form, RedirectAttributes redirect) {
        User user = users.create(form);
        redirect.addFlashAttribute("success", user.getName() + " əlavə edildi.");
        return "redirect:/users";
    }

    @GetMapping("/users/{id}")
    public String editUser(@PathVariable int id, Model model) {
        model.addAttribute("user", users.user(id));
        model.addAttribute("roles", Role.values());
        return "account/user-form";
    }

    @PostMapping("/users/{id}")
    public String update(@PathVariable int id, @ModelAttribute UserForm form, HttpServletRequest request,
                         RedirectAttributes redirect) {
        users.update(id, form, users.current(request));
        redirect.addFlashAttribute("success", "İstifadəçi yeniləndi.");
        return "redirect:/users";
    }

    @GetMapping("/settings")
    public String settings() {
        return "account/settings";
    }

    @PostMapping("/settings")
    public String saveSettings(@RequestParam(required = false) String companyName,
                               @RequestParam(required = false) BigDecimal expensePct,
                               @RequestParam(required = false) BigDecimal profitPct,
                               @RequestParam(required = false) Integer productionDays, RedirectAttributes redirect) {
        erp.saveSettings(companyName, expensePct, profitPct, productionDays);
        redirect.addFlashAttribute("success", "Parametrlər yadda saxlanıldı. Yeni sifarişlər bu dəyərlərlə başlayacaq.");
        return "redirect:/settings";
    }
}
