package com.mesproject.mescore.observability.controller;

import com.mesproject.mescore.observability.dto.MetricsSummaryResponse;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/admin/metrics")
public class MetricsController {

    private final MeterRegistry registry;

    public MetricsController(MeterRegistry registry) {
        this.registry = registry;
    }

    @GetMapping("/summary")
    public MetricsSummaryResponse summary() {
        var timers = registry.find("http.server.requests").timers();
        long total = 0;
        long total5xx = 0;
        double totalTimeSec = 0.0;
        double p95 = 0.0;

        for (Timer t : timers) {
            long c = t.count();
            total += c;
            totalTimeSec += t.totalTime(TimeUnit.SECONDS);

            String status = t.getId().getTag("status");
            if (status != null && status.startsWith("5")) {
                total5xx += c;
            }

            var snap = t.takeSnapshot();
            var p = snap.percentileValues();
            for (var pv : p) {
                if (pv.percentile() >= 0.95) {
                    double v = pv.value(TimeUnit.MILLISECONDS);
                    if (v > p95) p95 = v;
                }
            }
        }

        double rps = (totalTimeSec <= 0.0) ? 0.0 : (total / totalTimeSec);
        double errRate = (total <= 0) ? 0.0 : ((double) total5xx / (double) total);

        return new MetricsSummaryResponse(rps, total, total5xx, errRate, p95);
    }
}
