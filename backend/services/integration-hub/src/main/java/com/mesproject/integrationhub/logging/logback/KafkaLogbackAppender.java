package com.mesproject.integrationhub.logging.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.mesproject.contract.JacksonSupport;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class KafkaLogbackAppender extends AppenderBase<ILoggingEvent> {

    private KafkaProducer<String, String> producer;
    private String bootstrapServers;
    private String topic = "mes.logs";
    private String service = "integration-hub";
    private Level threshold = Level.INFO;

    public void setBootstrapServers(String bootstrapServers) { this.bootstrapServers = bootstrapServers; }
    public void setTopic(String topic) { this.topic = topic; }
    public void setService(String service) { this.service = service; }
    public void setThreshold(String threshold) { if (threshold != null) this.threshold = Level.toLevel(threshold, Level.INFO); }

    @Override
    public void start() {
        if (bootstrapServers == null || bootstrapServers.isBlank()) {
            bootstrapServers = System.getenv().getOrDefault("SPRING_KAFKA_BOOTSTRAP_SERVERS",
                    System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:29092"));
        }

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.LINGER_MS_CONFIG, "5");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        producer = new KafkaProducer<>(props);
        super.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (producer == null) return;
        if (!event.getLevel().isGreaterOrEqual(threshold)) return;

        String logger = event.getLoggerName();
        if (logger != null && (logger.startsWith("com.mesproject.integrationhub.logging") || logger.contains("KafkaLogbackAppender"))) {
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("ts", Instant.ofEpochMilli(event.getTimeStamp()));
        payload.put("service", service);
        payload.put("level", event.getLevel().toString());
        payload.put("logger", event.getLoggerName());
        payload.put("thread", event.getThreadName());
        payload.put("traceId", firstNonNull(MDC.get("traceId"), MDC.get("X-B3-TraceId"), MDC.get("trace_id")));
        payload.put("msg", event.getFormattedMessage());
        payload.put("exception", event.getThrowableProxy() == null ? null :
                event.getThrowableProxy().getClassName() + ": " + event.getThrowableProxy().getMessage());
        payload.put("meta", Map.of());

        try {
            String json = JacksonSupport.mapper().writeValueAsString(payload);
            producer.send(new ProducerRecord<>(topic, service, json));
        } catch (Exception ignored) {}
    }

    private static String firstNonNull(String... values) {
        for (String v : values) if (v != null && !v.isBlank()) return v;
        return null;
    }

    @Override
    public void stop() {
        super.stop();
        try { if (producer != null) producer.close(); } catch (Exception ignored) {}
    }
}
