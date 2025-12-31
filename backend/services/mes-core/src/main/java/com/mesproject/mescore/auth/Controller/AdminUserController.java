package com.mesproject.mescore.auth.Controller;

import com.mesproject.mescore.auth.dto.UserCreateRequest;
import com.mesproject.mescore.auth.dto.UserView;
import com.mesproject.mescore.auth.service.UserAdminService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserAdminService svc;

    public AdminUserController(UserAdminService svc) {
        this.svc = svc;
    }

    @GetMapping
    public List<UserView> list() {
        return svc.list();
    }

    @PostMapping
    public UserView create(@RequestBody UserCreateRequest req) {
        return svc.create(req);
    }

    @PostMapping("/<built-in function id>/enable")
    public UserView enable(@PathVariable long id, @RequestBody Map<String, Object> body) {
        boolean enabled = Boolean.TRUE.equals(body.get("enabled")) || "true".equals(String.valueOf(body.get("enabled")));
        return svc.setEnabled(id, enabled);
    }

    @PostMapping("/<built-in function id>/role")
    public UserView role(@PathVariable long id, @RequestBody Map<String, Object> body) {
        String role = String.valueOf(body.getOrDefault("role", "USER"));
        return svc.setRole(id, role);
    }
}
