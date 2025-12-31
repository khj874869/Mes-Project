package com.mesproject.mescore.persistence;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name="outbox_event")
public class OutboxEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="event_type", nullable=false, length=100)
    private String eventType;

    @Column(name="aggregate_id", nullable=false, length=100)
    private String aggregateId;

    @Column(name="payload_json", nullable=false, columnDefinition = "text")
    private String payloadJson;

    @Column(name="status", nullable=false, length=20)
    private String status = "NEW";

    @Column(name="created_at", nullable=false)
    private Instant createdAt = Instant.now();

    @Column(name="published_at")
    private Instant publishedAt;

    protected OutboxEvent() {}

    public OutboxEvent(String eventType, String aggregateId, String payloadJson) {
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.payloadJson = payloadJson;
    }

    public Long getId() { return id; }
    public String getEventType() { return eventType; }
    public String getAggregateId() { return aggregateId; }
    public String getPayloadJson() { return payloadJson; }
    public String getStatus() { return status; }

    public void markPublished() {
        this.status = "PUBLISHED";
        this.publishedAt = Instant.now();
    }
}
