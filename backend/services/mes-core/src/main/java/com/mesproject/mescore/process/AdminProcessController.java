package com.mesproject.mescore.process;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/admin/process")
public class AdminProcessController {

    private final ProcessRepository repo;

    public AdminProcessController(ProcessRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/sla")
    public List<ProcessRepository.StationSlaView> listSla() {
        return repo.listSlaWithStationName();
    }

    public static class UpsertSlaRequest {
        @NotBlank
        public String stationCode;
        @Min(1)
        public int maxCycleSec;
        @Min(1)
        public int maxStuckSec;
        @Min(0)
        public int manualCheckSec;
        @Min(0)
        public int autoCheckSec;
        @Min(1)
        public int idealCycleSec;
        @Min(1)
        public int manning;
    }

    @PostMapping("/sla")
    public ResponseEntity<?> upsert(@Valid @RequestBody UpsertSlaRequest req) {
        repo.upsertSla(new ProcessRepository.StationSlaView(
                req.stationCode.trim(),
                req.maxCycleSec,
                req.maxStuckSec,
                req.manualCheckSec,
                req.autoCheckSec,
                req.idealCycleSec,
                req.manning,
                Instant.now()
        ));
        return ResponseEntity.ok().build();
    }
}
