package com.mesproject.mescore.auth.dto;

public record MeResponse(
        String username,
        String displayName,
        String role
) {}
