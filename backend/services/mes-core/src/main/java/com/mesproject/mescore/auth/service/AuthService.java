package com.mesproject.mescore.auth.service;

import com.mesproject.mescore.auth.dto.*;
import com.mesproject.mescore.auth.repo.AppUserRepository;
import com.mesproject.mescore.security.JwtTokenProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AppUserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwt;

    public AuthService(AppUserRepository userRepo, PasswordEncoder encoder, JwtTokenProvider jwt) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public LoginResponse login(LoginRequest req) {
        var user = userRepo.findByUsername(req.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!user.isEnabled() || !encoder.matches(req.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        var authorities = user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority(r.getName()))
                .toList();

        String token = jwt.createToken(user.getUsername(), authorities);
        String role = user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getName())) ? "ADMIN" : "USER";

        return new LoginResponse(token, user.getUsername(), user.getDisplayName(), role);
    }
}
