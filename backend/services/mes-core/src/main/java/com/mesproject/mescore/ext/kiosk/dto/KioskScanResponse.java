package com.mesproject.mescore.ext.kiosk.dto;

import java.time.Instant;

public class KioskScanResponse {
    private String status; // OK | DUPLICATE
    private String eventId;
    private String idempotencyKey;
    private String tagId;
    private String stationCode;
    private String direction;
    private Instant occurredAt;

    public KioskScanResponse() {}

    public KioskScanResponse(
            String status,
            String eventId,
            String idempotencyKey,
            String tagId,
            String stationCode,
            String direction,
            Instant occurredAt
    ) {
        this.status = status;
        this.eventId = eventId;
        this.idempotencyKey = idempotencyKey;
        this.tagId = tagId;
        this.stationCode = stationCode;
        this.direction = direction;
        this.occurredAt = occurredAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getStationCode() {
        return stationCode;
    }

    public void setStationCode(String stationCode) {
        this.stationCode = stationCode;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }
}
