package com.mesproject.mescore.ext.system;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.mesproject.mescore.ext.system.SystemDtos.LineView;
import static com.mesproject.mescore.ext.system.SystemDtos.StationView;

/**
 * 인증 없이 조회 가능한 시스템 마스터(스테이션 목록 등).
 * - Kiosk에서 스테이션 리스트를 DB에서 불러오기 위해 사용
 */
@RestController
@RequestMapping("/system")
public class SystemController {

    private final SystemMasterRepository repo;

    public SystemController(SystemMasterRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/lines")
    public List<LineView> lines(
            @RequestParam(name = "active", defaultValue = "true") boolean activeOnly
    ) {
        return repo.findLines(activeOnly);
    }

    @GetMapping("/stations")
    public List<StationView> stations(
            @RequestParam(name = "active", defaultValue = "true") boolean activeOnly,
            @RequestParam(name = "kiosk", defaultValue = "false") boolean kioskOnly
    ) {
        return repo.findStations(activeOnly, kioskOnly);
    }
}
