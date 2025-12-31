package com.mesproject.mescore.telemetry.dto;

import java.time.Instant;

public record TelemetryReadingEvent(
        String siteId,
        String sensorId,
        String metric, // TEMP/HUMIDITY/POWER
        double value,
        Instant ts
) {}
