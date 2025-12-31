package com.mesproject.mescore.ext.system;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.mesproject.mescore.ext.system.SystemDtos.*;

/**
 * 관리자 전용: 라인/스테이션 마스터 저장
 */
@RestController
@RequestMapping("/admin/system")
public class AdminSystemController {

    private final SystemMasterRepository repo;

    public AdminSystemController(SystemMasterRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/lines")
    public List<LineView> listLines() {
        return repo.findLines(false);
    }

    @PostMapping("/lines")
    public ResponseEntity<?> upsertLine(@Valid @RequestBody UpsertLineRequest req) {
        repo.upsertLine(req.getLineCode().trim(), req.getName().trim(), Boolean.TRUE.equals(req.getActive()));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stations")
    public List<StationView> listStations() {
        return repo.findStations(false, false);
    }

    @PostMapping("/stations")
    public ResponseEntity<?> upsertStation(@Valid @RequestBody UpsertStationRequest req) {
        repo.upsertStation(
                req.getStationCode().trim(),
                req.getName().trim(),
                req.getLineCode().trim(),
                req.getSeq(),
                Boolean.TRUE.equals(req.getKioskEnabled()),
                Boolean.TRUE.equals(req.getActive())
        );
        return ResponseEntity.ok().build();
    }
}
