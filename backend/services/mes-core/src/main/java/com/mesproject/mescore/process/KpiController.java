package com.mesproject.mescore.process;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/kpi")
public class KpiController {

    private final KpiService kpiService;
    private final ProcessRepository repo;
    private final KpiExcelExporter exporter;

    public KpiController(KpiService kpiService, ProcessRepository repo, KpiExcelExporter exporter) {
        this.kpiService = kpiService;
        this.repo = repo;
        this.exporter = exporter;
    }

    @GetMapping("/overview")
    public KpiService.Overview overview(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to
    ) {
        if (from == null || to == null) {
            Instant now = Instant.now();
            to = now;
            from = now.minus(Duration.ofHours(24));
        }
        return kpiService.overview(from, to);
    }

    @GetMapping(value = "/report.xlsx")
    public ResponseEntity<byte[]> report(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to
    ) {
        KpiService.Overview ov = overview(from, to);

        List<ProcessRepository.AlarmView> alarms = repo.listAlarmsBetween(ov.from(), ov.to(), 5000);
        byte[] bytes = exporter.export(ov, alarms);

        String filename = "mes_kpi_" + ov.from().toString().replace(":", "-") + "_" + ov.to().toString().replace(":", "-") + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }
}
