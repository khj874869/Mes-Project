package com.mesproject.mescore.auth.dto;

public record LoginRequest(
        String username,
        String password
) {}
