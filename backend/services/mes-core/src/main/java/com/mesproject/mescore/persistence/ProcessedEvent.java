package com.mesproject.mescore.persistence;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "processed_event", uniqueConstraints = @UniqueConstraint(name="uk_processed_idem", columnNames = "idempotency_key"))
public class ProcessedEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="idempotency_key", nullable=false, length=200)
    private String idempotencyKey;

    @Column(name="processed_at", nullable=false)
    private Instant processedAt = Instant.now();

    protected ProcessedEvent() {}

    public ProcessedEvent(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getIdempotencyKey() { return idempotencyKey; }

}
