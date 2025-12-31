package com.mesproject.mescore.process;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class StuckWipScanner {

    private final ProcessRepository repo;
    private final ProcessAutomationService automation;

    public StuckWipScanner(ProcessRepository repo, ProcessAutomationService automation) {
        this.repo = repo;
        this.automation = automation;
    }

    /**
     * 정체(STUCK) 감지:
     * - wip_unit.last_station 기준으로 최근 IN 이후 OUT이 없고, max_stuck_sec 초과하면 알람 생성
     */
    @Scheduled(fixedDelayString = "${mes.process.stuckScanIntervalMs:30000}")
    public void scan() {
        Instant now = Instant.now();
        for (var u : repo.listUnitsWithStation(2000)) {
            String tagId = u.tagId();
            String stationCode = u.stationCode();
            var slaOpt = repo.findSla(stationCode);
            if (slaOpt.isEmpty()) continue;
            var sla = slaOpt.get();

            var inAtOpt = repo.findLastInAt(tagId, stationCode);
            if (inAtOpt.isEmpty()) continue;
            Instant inAt = inAtOpt.get();

            if (repo.existsOutAfter(tagId, stationCode, inAt)) {
                continue; // 이미 OUT 처리됨
            }

            long stuckSec = Duration.between(inAt, now).getSeconds();
            if (stuckSec <= sla.maxStuckSec()) continue;

            String severity = stuckSec > sla.maxStuckSec() * 2L ? "CRIT" : "WARN";
            String msg = String.format("공정 정체 감지: %ds (SLA %ds)", stuckSec, sla.maxStuckSec());

            repo.insertAlarmIfAbsent(
                    "STUCK",
                    severity,
                    "OPEN",
                    tagId,
                    stationCode,
                    msg,
                    inAt,
                    "SYSTEM"
            ).ifPresent(automation::broadcastAlarm);
        }
    }
}
