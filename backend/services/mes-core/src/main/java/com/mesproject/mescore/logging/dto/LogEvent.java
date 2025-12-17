package com.mesproject.mescore.logging.dto;

import java.time.Instant;
import java.util.Map;

public record LogEvent(
        Instant ts,
        String service,
        String level,
        String logger,
        String thread,
        String traceId,
        String msg,
        String exception,
        Map<String, Object> meta
) {}
