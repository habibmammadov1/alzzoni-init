package az.beit.alzoni.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import az.beit.alzoni.domain.Role;
import az.beit.alzoni.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** Users of the system. There is no login yet: the current user is simply whoever is picked on the profile page. */
@Service
public class UserService {

    private static final String SESSION_KEY = "userId";

    private final Map<Integer, User> users = new ConcurrentSkipListMap<>();
    private int seq;

    public Collection<User> users() {
        return users.values();
    }

    public List<User> activeUsers() {
        return users.values().stream().filter(User::isActive).toList();
    }

    public User user(int id) {
        User user = users.get(id);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return user;
    }

    public User current(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object id = session == null ? null : session.getAttribute(SESSION_KEY);
        User user = id == null ? null : users.get(id);
        return user != null && user.isActive() ? user : activeUsers().getFirst();
    }

    public void switchTo(int id, HttpServletRequest request) {
        User user = user(id);
        if (!user.isActive()) {
            throw new IllegalArgumentException("Bu istifadəçi deaktivdir.");
        }
        request.getSession().setAttribute(SESSION_KEY, id);
    }

    public synchronized User create(UserForm form) {
        checkContact(form.name());
        if (form.role() == null) {
            throw new IllegalArgumentException("Rol seçin.");
        }
        User user = new User(++seq, form.name().trim(), form.role(), form.phone(), form.email());
        user.setActive(!Boolean.FALSE.equals(form.active()));
        users.put(user.getId(), user);
        return user;
    }

    public synchronized void update(int id, UserForm form, User current) {
        User user = user(id);
        boolean active = Boolean.TRUE.equals(form.active());
        checkContact(form.name());
        if (form.role() == null) {
            throw new IllegalArgumentException("Rol seçin.");
        }
        if (user == current && !active) {
            throw new IllegalStateException("Hazırda işlədiyiniz istifadəçini deaktiv etmək olmaz.");
        }
        if (user.isActive() && !active && user.getRole() == Role.ADMIN && activeAdmins() == 1) {
            throw new IllegalStateException("Sistemdə ən azı bir aktiv administrator qalmalıdır.");
        }
        user.setName(form.name().trim());
        user.setRole(form.role());
        user.setPhone(form.phone());
        user.setEmail(form.email());
        user.setActive(active);
    }

    public synchronized void updateContact(User user, String name, String phone, String email) {
        checkContact(name);
        user.setName(name.trim());
        user.setPhone(phone);
        user.setEmail(email);
    }

    private long activeAdmins() {
        return activeUsers().stream().filter(u -> u.getRole() == Role.ADMIN).count();
    }

    private static void checkContact(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Ad və soyad mütləqdir.");
        }
    }
}
