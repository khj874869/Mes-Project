package com.mesproject.mescore.auth.dto;

import java.time.OffsetDateTime;

public record UserView(
        Long id,
        String username,
        String displayName,
        boolean enabled,
        String role,
        OffsetDateTime createdAt
) {}
