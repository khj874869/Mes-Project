package com.mesproject.contract;

import java.time.Instant;

public record WipMovedEvent(
        String eventId,
        Instant occurredAt,
        String idempotencyKey,
        String serialNo,
        String fromStation,
        String toStation
) {}
