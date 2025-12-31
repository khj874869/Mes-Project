package com.mesproject.rfidgateway.api;

import com.mesproject.contract.RfidRawEvent;
import com.mesproject.contract.Topics;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/rfid")
public class RfidEventController {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public RfidEventController(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/events")
    public ResponseEntity<?> ingest(@RequestHeader(value = "X-Request-Id", required = false) String requestId,
                                    @Valid @RequestBody RfidEventRequest req) {

        String eid = UUID.randomUUID().toString();
        String idempotencyKey = (StringUtils.hasText(requestId))
                ? "rfid:" + requestId
                : "rfid:" + req.deviceId() + ":" + req.tagId() + ":" + req.seq();

        var event = new RfidRawEvent(
                eid,
                Instant.now(),
                idempotencyKey,
                req.deviceId(),
                req.tagId(),
                req.stationCode(),
                req.direction(),
                req.seq()
        );

        kafkaTemplate.send(Topics.RFID_RAW, req.tagId(), event);

        return ResponseEntity.accepted().body(java.util.Map.of(
                "status", "ACCEPTED",
                "eventId", eid,
                "idempotencyKey", idempotencyKey
        ));
    }
}
