package com.mesproject.integrationhub.persistence;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name="interface_message", uniqueConstraints = @UniqueConstraint(name="uk_if_idem", columnNames = "idempotency_key"))
public class InterfaceMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="idempotency_key", nullable=false, length=200)
    private String idempotencyKey;

    @Column(name="direction", nullable=false, length=20)
    private String direction;

    @Column(name="system_name", nullable=false, length=30)
    private String systemName;

    @Column(name="message_type", nullable=false, length=50)
    private String messageType;

    @Column(name="payload_json", nullable=false, columnDefinition="text")
    private String payloadJson;

    @Column(name="created_at", nullable=false)
    private Instant createdAt = Instant.now();

    protected InterfaceMessage() {}

    public InterfaceMessage(String idempotencyKey, String direction, String systemName, String messageType, String payloadJson) {
        this.idempotencyKey = idempotencyKey;
        this.direction = direction;
        this.systemName = systemName;
        this.messageType = messageType;
        this.payloadJson = payloadJson;
    }

    public Long getId() { return id; }
}
