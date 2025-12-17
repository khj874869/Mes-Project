package com.mesproject.mescore.telemetry.controller;

import com.mesproject.mescore.telemetry.dto.TelemetryReadingEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

import static com.mesproject.contract.Topics.TELEMETRY_RAW;

@RestController
@RequestMapping("/telemetry")
public class TelemetryIngestController {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${telemetry.ingest.api-key:change-me}")
    private String apiKey;

    public TelemetryIngestController(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/ingest")
    public void ingest(@RequestHeader(value="X-API-KEY", required=false) String key,
                       @RequestBody TelemetryReadingEvent e) {
        if (apiKey != null && !apiKey.isBlank()) {
            if (key == null || !apiKey.equals(key)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid api key");
            }
        }
        TelemetryReadingEvent normalized = new TelemetryReadingEvent(
                e.siteId(),
                e.sensorId(),
                e.metric(),
                e.value(),
                e.ts() != null ? e.ts() : Instant.now()
        );
        kafkaTemplate.send(TELEMETRY_RAW, normalized.siteId(), normalized);
    }
}
