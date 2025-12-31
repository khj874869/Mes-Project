package com.mesproject.mescore.logging.kafka;

import com.mesproject.contract.JacksonSupport;
import com.mesproject.contract.Topics;
import com.mesproject.mescore.logging.dto.LogEvent;
import com.mesproject.mescore.logging.sse.SseHub;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class LogEventConsumer {

    private final SseHub hub;

    public LogEventConsumer(SseHub hub) {
        this.hub = hub;
    }

    @KafkaListener(topics = Topics.LOGS, groupId = "mes-core-log-dashboard")
    public void onMessage(String json) {
        try {
            LogEvent event = JacksonSupport.mapper().readValue(json, LogEvent.class);
            hub.broadcast("log", event);
        } catch (Exception ignored) {
        }
    }
}
