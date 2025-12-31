package com.mesproject.mescore.process;

import com.mesproject.mescore.logging.sse.SseHub;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ProcessAutomationService {

    private final ProcessRepository repo;
    private final SseHub sseHub;

    public ProcessAutomationService(ProcessRepository repo, Optional<SseHub> sseHub) {
        this.repo = repo;
        this.sseHub = sseHub.orElse(null);
    }

    /**
     * 무인 공정 체크(스캔 기반)
     * - OUT 시점에 cycle time 계산 후 SLA 초과 => 알람 생성
     */
    public void onScan(String tagId, String stationCode, String direction, Instant occurredAt, String actor) {
        if (tagId == null || stationCode == null || direction == null || occurredAt == null) return;
        String dir = direction.trim().toUpperCase();
        if (!dir.equals("OUT")) return;

        var slaOpt = repo.findSla(stationCode);
        if (slaOpt.isEmpty()) return;
        var sla = slaOpt.get();

        Double cycleSec = repo.findCycleSecForOut(tagId, stationCode, occurredAt).orElse(null);
        if (cycleSec == null || cycleSec.isNaN() || cycleSec < 0) return;

        if (cycleSec > sla.maxCycleSec()) {
            String severity = cycleSec > sla.maxCycleSec() * 2L ? "CRIT" : "WARN";
            String msg = String.format("CycleTime SLA 초과: %.0fs (SLA %ds)", cycleSec, sla.maxCycleSec());
            var alarm = repo.insertAlarmIfAbsent(
                    "CYCLE_EXCEEDED",
                    severity,
                    "OPEN",
                    tagId,
                    stationCode,
                    msg,
                    occurredAt,
                    actor
            );
            alarm.ifPresent(this::broadcastAlarm);
        }
    }

    public void broadcastAlarm(ProcessRepository.AlarmView alarm) {
        if (sseHub == null || alarm == null) return;
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", alarm.id());
        payload.put("alarmType", alarm.alarmType());
        payload.put("severity", alarm.severity());
        payload.put("status", alarm.status());
        payload.put("tagId", alarm.tagId());
        payload.put("stationCode", alarm.stationCode());
        payload.put("lineCode", alarm.lineCode());
        payload.put("message", alarm.message());
        payload.put("occurredAt", alarm.occurredAt() == null ? null : alarm.occurredAt().toString());
        payload.put("detectedAt", alarm.detectedAt() == null ? null : alarm.detectedAt().toString());
        payload.put("assignedTo", alarm.assignedTo());
        sseHub.broadcast("alarm", payload);
    }

    public void broadcastAlarmStatusChange(long alarmId, String action, String actor) {
        if (sseHub == null) return;
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", alarmId);
        payload.put("action", action);
        payload.put("actor", actor);
        payload.put("ts", Instant.now().toString());
        sseHub.broadcast("alarm", payload);
    }
}
