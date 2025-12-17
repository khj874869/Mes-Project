package com.mesproject.mescore.observability.dto;

public record MetricsSummaryResponse(
        double avgRpsSinceStart,
        long totalRequests,
        long total5xx,
        double errorRate,
        double p95Ms
) {}
