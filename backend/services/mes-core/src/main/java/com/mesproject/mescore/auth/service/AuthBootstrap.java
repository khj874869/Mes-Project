package com.mesproject.mescore.auth.service;

import com.mesproject.mescore.auth.domain.AppUser;
import com.mesproject.mescore.auth.repo.AppRoleRepository;
import com.mesproject.mescore.auth.repo.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuthBootstrap implements ApplicationRunner {

    private final AppUserRepository userRepo;
    private final AppRoleRepository roleRepo;
    private final PasswordEncoder encoder;

    @Value("${security.bootstrap-admin.enabled:true}")
    private boolean enabled;

    @Value("${security.bootstrap-admin.username:admin}")
    private String adminUsername;

    @Value("${security.bootstrap-admin.password:admin1234}")
    private String adminPassword;

    public AuthBootstrap(AppUserRepository userRepo, AppRoleRepository roleRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) return;

        // If no users exist, create default admin
        if (userRepo.count() > 0) return;

        var adminRole = roleRepo.findByName("ROLE_ADMIN").orElseThrow();
        AppUser admin = new AppUser(adminUsername, encoder.encode(adminPassword), "Administrator");
        admin.addRole(adminRole);
        userRepo.save(admin);
    }
}
