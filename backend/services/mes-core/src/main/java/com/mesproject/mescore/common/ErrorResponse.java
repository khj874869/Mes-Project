package com.mesproject.mescore.common;

import java.time.Instant;

public record ErrorResponse(
        Instant ts,
        int status,
        String error,
        String message,
        String path
) {}
