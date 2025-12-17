package com.mesproject.mescore.production.dto;

import java.time.Instant;

public record ProductionResultEvent(
        String workOrderNo,
        String lineCode,
        String stationCode,
        String itemCode,
        int qtyGood,
        int qtyNg,
        Instant startedAt,
        Instant endedAt,
        Instant createdAt
) {}
