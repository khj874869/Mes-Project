package com.mesproject.contract;

import java.time.Instant;
import java.time.LocalDate;

public record ErpWorkOrderUpsertEvent(
        String eventId,
        Instant occurredAt,
        String idempotencyKey,
        String woNo,
        String itemCode,
        int quantity,
        LocalDate dueDate
) {}
