package com.mesproject.mescore.telemetry.controller;

import com.mesproject.mescore.telemetry.domain.TelemetryReading;
import com.mesproject.mescore.telemetry.dto.TelemetryReadingEvent;
import com.mesproject.mescore.telemetry.repo.TelemetryReadingRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

import static com.mesproject.contract.Topics.TELEMETRY_RAW;

@RestController
@RequestMapping("/admin/telemetry")
public class TelemetryAdminController {

    private final TelemetryReadingRepository repo;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TelemetryAdminController(TelemetryReadingRepository repo, KafkaTemplate<String, Object> kafkaTemplate) {
        this.repo = repo;
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping("/recent")
    public List<TelemetryReading> recent(@RequestParam String siteId, @RequestParam(defaultValue = "200") int limit) {
        int safe = Math.min(Math.max(limit, 1), 2000);
        return repo.findRecent(siteId, safe);
    }

    @GetMapping("/range")
    public List<TelemetryReading> range(@RequestParam String siteId, @RequestParam Instant from, @RequestParam Instant to) {
        return repo.findRange(siteId, from, to);
    }

    // Admin용 테스트/시뮬레이터: Kafka로 넣기
    @PostMapping("/publish")
    public void publish(@RequestBody TelemetryReadingEvent e) {
        kafkaTemplate.send(TELEMETRY_RAW, e.siteId(), e);
    }
}
