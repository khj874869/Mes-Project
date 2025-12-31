package com.mesproject.mescore.telemetry.kafka;

import com.mesproject.contract.Topics;
import com.mesproject.mescore.logging.sse.SseHub;
import com.mesproject.mescore.telemetry.domain.TelemetryReading;
import com.mesproject.mescore.telemetry.dto.TelemetryReadingEvent;
import com.mesproject.mescore.telemetry.repo.TelemetryReadingRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TelemetryConsumer {

    private final TelemetryReadingRepository repo;
    private final SseHub hub;

    public TelemetryConsumer(TelemetryReadingRepository repo, SseHub hub) {
        this.repo = repo;
        this.hub = hub;
    }

    @KafkaListener(topics = Topics.TELEMETRY_RAW, groupId = "mes-core-telemetry")
    public void onMessage(TelemetryReadingEvent e) {
        repo.save(new TelemetryReading(e.siteId(), e.sensorId(), e.metric(), e.value(), e.ts()));
        hub.broadcast("telemetry", e);
    }
}
