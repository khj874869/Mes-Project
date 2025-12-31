package com.mesproject.mescore.auth.dto;

public record UserCreateRequest(
        String username,
        String password,
        String displayName,
        String role // ADMIN or USER
) {}
