package com.mesproject.mescore.auth.service;

import com.mesproject.mescore.auth.domain.AppRole;
import com.mesproject.mescore.auth.domain.AppUser;
import com.mesproject.mescore.auth.dto.UserCreateRequest;
import com.mesproject.mescore.auth.dto.UserView;
import com.mesproject.mescore.auth.repo.AppRoleRepository;
import com.mesproject.mescore.auth.repo.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserAdminService {

    private final AppUserRepository userRepo;
    private final AppRoleRepository roleRepo;
    private final PasswordEncoder encoder;

    public UserAdminService(AppUserRepository userRepo, AppRoleRepository roleRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
    }

    @Transactional
    public UserView create(UserCreateRequest req) {
        if (userRepo.existsByUsername(req.username())) {
            throw new IllegalArgumentException("username already exists");
        }

        AppUser user = new AppUser(req.username(), encoder.encode(req.password()), req.displayName());
        AppRole role = roleRepo.findByName("ADMIN".equalsIgnoreCase(req.role()) ? "ROLE_ADMIN" : "ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("role missing"));
        user.addRole(role);
        userRepo.save(user);
        return toView(user);
    }

    public List<UserView> list() {
        return userRepo.findAll().stream().map(this::toView).toList();
    }

    @Transactional
    public UserView setEnabled(long userId, boolean enabled) {
        var user = userRepo.findById(userId).orElseThrow();
        user.setEnabled(enabled);
        return toView(user);
    }

    @Transactional
    public UserView setRole(long userId, String role) {
        var user = userRepo.findById(userId).orElseThrow();
        user.getRoles().clear();
        AppRole r = roleRepo.findByName("ADMIN".equalsIgnoreCase(role) ? "ROLE_ADMIN" : "ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("role missing"));
        user.addRole(r);
        return toView(user);
    }

    private UserView toView(AppUser u) {
        String role = u.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getName())) ? "ADMIN" : "USER";
        return new UserView(u.getId(), u.getUsername(), u.getDisplayName(), u.isEnabled(), role, u.getCreatedAt());
    }
}
