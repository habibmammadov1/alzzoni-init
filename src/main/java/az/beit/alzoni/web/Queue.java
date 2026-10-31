package az.beit.alzoni.web;

import java.util.List;

import org.springframework.ui.Model;

import az.beit.alzoni.domain.Order;

/** Fills the shared queue page: the orders waiting at one stage of the flow. */
final class Queue {

    private Queue() {
    }

    static String show(Model model, int step, String title, String hint, List<Order> orders, String basePath, String action) {
        model.addAttribute("step", step);
        model.addAttribute("title", title);
        model.addAttribute("hint", hint);
        model.addAttribute("orders", orders);
        model.addAttribute("basePath", basePath);
        model.addAttribute("action", action);
        return "queue";
    }
}
