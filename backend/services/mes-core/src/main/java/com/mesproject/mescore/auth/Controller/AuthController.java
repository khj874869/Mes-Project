package com.mesproject.mescore.auth.Controller;

import com.mesproject.mescore.auth.dto.*;
import com.mesproject.mescore.auth.repo.AppUserRepository;
import com.mesproject.mescore.auth.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final AppUserRepository userRepo;

    public AuthController(AuthService authService, AppUserRepository userRepo) {
        this.authService = authService;
        this.userRepo = userRepo;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/me")
    public MeResponse me(Authentication auth) {
        if (auth == null) return null;
        var user = userRepo.findByUsername(auth.getName()).orElseThrow();
        String role = user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getName())) ? "ADMIN" : "USER";
        return new MeResponse(user.getUsername(), user.getDisplayName(), role);
    }
}
