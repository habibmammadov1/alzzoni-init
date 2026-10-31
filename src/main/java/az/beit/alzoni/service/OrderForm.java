package az.beit.alzoni.service;

import java.time.LocalDate;

import az.beit.alzoni.domain.OrderType;

public record OrderForm(String customer, String phone, String manager, Boolean specialCustomer,
                        OrderType type, String recipeCode, String product, Integer qty,
                        LocalDate requestedDate, String note) {
}
