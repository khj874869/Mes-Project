package com.mesproject.mescore.process;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/alarms")
public class AlarmController {

    private final ProcessRepository repo;
    private final ProcessAutomationService automation;

    public AlarmController(ProcessRepository repo, ProcessAutomationService automation) {
        this.repo = repo;
        this.automation = automation;
    }

    private static String actor() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return (a == null || a.getName() == null) ? "SYSTEM" : a.getName();
    }

    @GetMapping
    public List<ProcessRepository.AlarmView> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "200") int limit
    ) {
        return repo.listAlarms(status, limit);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable long id) {
        return repo.findAlarm(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/history")
    public List<ProcessRepository.AlarmHistoryView> history(@PathVariable long id) {
        return repo.listHistory(id);
    }

    @PostMapping("/{id}/ack")
    public ResponseEntity<?> ack(@PathVariable long id) {
        boolean ok = repo.ackAlarm(id, actor());
        if (ok) automation.broadcastAlarmStatusChange(id, "ACK", actor());
        return ResponseEntity.ok(Map.of("ok", ok));
    }

    public static class AssignRequest {
        public String assignedTo;
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<?> assign(@PathVariable long id, @RequestBody AssignRequest req) {
        String assignee = (req == null || req.assignedTo == null) ? null : req.assignedTo.trim();
        boolean ok = repo.assignAlarm(id, assignee, actor());
        if (ok) automation.broadcastAlarmStatusChange(id, "ASSIGN", actor());
        return ResponseEntity.ok(Map.of("ok", ok));
    }

    public static class CloseRequest {
        public String note;
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<?> close(@PathVariable long id, @RequestBody(required = false) CloseRequest req) {
        String note = (req == null) ? null : req.note;
        boolean ok = repo.closeAlarm(id, actor(), note);
        if (ok) automation.broadcastAlarmStatusChange(id, "CLOSE", actor());
        return ResponseEntity.ok(Map.of("ok", ok));
    }
}
