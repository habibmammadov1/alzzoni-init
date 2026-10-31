package az.beit.alzoni.service;

import az.beit.alzoni.domain.Role;

public record UserForm(String name, Role role, String phone, String email, Boolean active) {
}
