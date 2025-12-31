package com.mesproject.mescore.ext.kiosk.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Kiosk(현장) 스캔 요청.
 *
 * <pre>
 * {
 *   "tagId": "TAG-0001",
 *   "stationCode": "S01",
 *   "direction": "IN",
 *   "occurredAt": "2025-12-23T12:34:56Z"  // optional
 * }
 * </pre>
 */
public class KioskScanRequest {

    @NotBlank
    private String tagId;

    @NotBlank
    private String stationCode;

    /** IN / OUT */
    @NotBlank
    private String direction;

    /** ISO-8601 Instant (optional). 미전송 시 서버 시간이 사용됩니다. */
    private String occurredAt;

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

    public String getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(String occurredAt) {
        this.occurredAt = occurredAt;
    }
}
