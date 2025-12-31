package com.mesproject.mescore.ext.kiosk;

import com.mesproject.mescore.ext.kiosk.dto.KioskScanRequest;
import com.mesproject.mescore.ext.kiosk.dto.KioskScanResponse;
import com.mesproject.mescore.logging.sse.SseHub;
import com.mesproject.mescore.process.ProcessAutomationService;
import com.mesproject.mescore.persistence.ProcessedEvent;
import com.mesproject.mescore.persistence.WipEvent;
import com.mesproject.mescore.persistence.WipUnit;
import com.mesproject.mescore.persistence.repo.ProcessedEventRepository;
import com.mesproject.mescore.persistence.repo.WipEventRepository;
import com.mesproject.mescore.persistence.repo.WipUnitRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Kiosk용 최소 API.
 * - /kiosk/scan/in, /kiosk/scan/out : 스캔 이벤트를 DB(wip_event, processed_event)에 저장 + wip_unit 이동
 */
@RestController
@RequestMapping("/kiosk")
public class KioskController {

    private final ProcessedEventRepository processedRepo;
    private final WipUnitRepository wipUnitRepo;
    private final WipEventRepository wipEventRepo;
    private final SseHub sseHub;
    private final ProcessAutomationService automation;

    public KioskController(
            ProcessedEventRepository processedRepo,
            WipUnitRepository wipUnitRepo,
            WipEventRepository wipEventRepo,
            Optional<SseHub> sseHub,
            ProcessAutomationService automation
    ) {
        this.processedRepo = processedRepo;
        this.wipUnitRepo = wipUnitRepo;
        this.wipEventRepo = wipEventRepo;
        this.sseHub = sseHub.orElse(null);
        this.automation = automation;
    }

    @PostMapping("/scan/in")
    @Transactional
    public ResponseEntity<KioskScanResponse> scanIn(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @Valid @RequestBody KioskScanRequest req
    ) {
        return ResponseEntity.ok(handleScan(requestId, req, "IN"));
    }

    @PostMapping("/scan/out")
    @Transactional
    public ResponseEntity<KioskScanResponse> scanOut(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @Valid @RequestBody KioskScanRequest req
    ) {
        return ResponseEntity.ok(handleScan(requestId, req, "OUT"));
    }

    private KioskScanResponse handleScan(String requestId, KioskScanRequest req, String fallbackDirection) {
        final String tagId = req.getTagId().trim();
        final String stationCode = req.getStationCode().trim();
        final String direction = normalizeDirection(req.getDirection(), fallbackDirection);
        final Instant occurredAt = parseOccurredAt(req.getOccurredAt());

        final String idempotencyKey = buildIdempotencyKey(requestId, tagId, stationCode, direction, occurredAt);

        if (processedRepo.findByIdempotencyKey(idempotencyKey).isPresent()) {
            return new KioskScanResponse(
                    "DUPLICATE",
                    null,
                    idempotencyKey,
                    tagId,
                    stationCode,
                    direction,
                    occurredAt
            );
        }

        final String eventId = UUID.randomUUID().toString();

        // 이벤트 저장
        wipEventRepo.save(new WipEvent(
                eventId,
                idempotencyKey,
                tagId,
                stationCode,
                direction,
                occurredAt
        ));

        // WIP unit upsert + 이동
        WipUnit unit = wipUnitRepo.findByTagId(tagId)
                .orElseGet(() -> new WipUnit(tagId, tagId));
        unit.moveTo(stationCode);
        wipUnitRepo.save(unit);

        // 멱등 키 저장
        processedRepo.save(new ProcessedEvent(idempotencyKey));

        // SSE broadcast (선택)
        if (sseHub != null) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("eventId", eventId);
            payload.put("tagId", tagId);
            payload.put("stationCode", stationCode);
            payload.put("direction", direction);
            payload.put("occurredAt", occurredAt.toString());
            sseHub.broadcast("wip", payload);
        }

        // 무인 공정 체크(OUT 시 SLA 초과/정체 등 자동 알람)
        automation.onScan(tagId, stationCode, direction, occurredAt, "KIOSK");

        return new KioskScanResponse(
                "OK",
                eventId,
                idempotencyKey,
                tagId,
                stationCode,
                direction,
                occurredAt
        );
    }

    private static String normalizeDirection(String raw, String fallback) {
        String v = StringUtils.hasText(raw) ? raw.trim().toUpperCase(Locale.ROOT) : fallback;
        if (!v.equals("IN") && !v.equals("OUT")) return fallback;
        return v;
    }

    private static Instant parseOccurredAt(String occurredAt) {
        try {
            if (StringUtils.hasText(occurredAt)) return Instant.parse(occurredAt.trim());
        } catch (Exception ignored) {
        }
        return Instant.now();
    }

    private static String buildIdempotencyKey(
            String requestId,
            String tagId,
            String stationCode,
            String direction,
            Instant occurredAt
    ) {
        if (StringUtils.hasText(requestId)) return requestId.trim();

        // 프론트에서 헤더를 안 주는 경우에도, 최소한 같은 초에 같은 태그/스테이션/방향은 중복으로 본다.
        long bucket = occurredAt.getEpochSecond();
        return "kiosk:" + stationCode + ":" + direction + ":" + tagId + ":" + bucket;
    }
}
