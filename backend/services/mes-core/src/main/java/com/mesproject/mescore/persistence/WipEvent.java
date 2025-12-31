package com.mesproject.mescore.persistence;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name="wip_event")
public class WipEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="event_id", nullable=false, length=100)
    private String eventId;

    @Column(name="idempotency_key", nullable=false, length=200)
    private String idempotencyKey;

    @Column(name="tag_id", nullable=false, length=100)
    private String tagId;

    @Column(name="station_code", nullable=false, length=50)
    private String stationCode;

    @Column(name="direction", length=10)
    private String direction;

    @Column(name="occurred_at", nullable=false)
    private Instant occurredAt;

    protected WipEvent() {}

    public WipEvent(String eventId, String idempotencyKey, String tagId, String stationCode, String direction, Instant occurredAt) {
        this.eventId = eventId;
        this.idempotencyKey = idempotencyKey;
        this.tagId = tagId;
        this.stationCode = stationCode;
        this.direction = direction;
        this.occurredAt = occurredAt;
    }
}
