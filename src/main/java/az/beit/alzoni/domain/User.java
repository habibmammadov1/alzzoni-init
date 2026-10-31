package az.beit.alzoni.domain;

import java.util.Arrays;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
    private final int id;
    private String name;
    private Role role;
    private String phone;
    private String email;
    private boolean active = true;

    public User(int id, String name, Role role, String phone, String email) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.phone = phone;
        this.email = email;
    }

    public String getInitials() {
        return Arrays.stream(name.trim().split("\\s+")).limit(2)
                .map(part -> part.substring(0, 1)).collect(Collectors.joining());
    }
}
