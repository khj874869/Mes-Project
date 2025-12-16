package com.mesproject.contract;

import java.time.Instant;

public record RfidNormalizedEvent(
        String eventId,
        Instant occurredAt,
        String idempotencyKey,
        String tagId,
        String stationCode,
        String direction
) {}
