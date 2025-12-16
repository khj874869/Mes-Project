package com.mesproject.contract;

import java.time.Instant;

public record RfidRawEvent(
        String eventId,
        Instant occurredAt,
        String idempotencyKey,
        String deviceId,
        String tagId,
        String stationCode,
        String direction,
        long seq
) {}
