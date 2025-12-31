package com.mesproject.mescore.ext.wip;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wip")
public class WipQueryController {

    private final JdbcTemplate jdbc;

    public WipQueryController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record WipUnitView(String tagId, String serialNo, String lastStation) {}

    public record WipEventView(String eventId, String tagId, String stationCode, String direction, Instant occurredAt) {}

    public record StationCount(String stationCode, long qty) {}

    @GetMapping("/units")
    public List<WipUnitView> units(@RequestParam(name = "limit", defaultValue = "500") int limit) {
        int safe = Math.max(1, Math.min(limit, 2000));
        return jdbc.query(
                "SELECT tag_id, serial_no, last_station FROM wip_unit ORDER BY id DESC LIMIT ?",
                (rs, i) -> new WipUnitView(
                        rs.getString("tag_id"),
                        rs.getString("serial_no"),
                        rs.getString("last_station")
                ),
                safe
        );
    }

    @GetMapping("/events")
    public List<WipEventView> events(@RequestParam(name = "limit", defaultValue = "100") int limit) {
        int safe = Math.max(1, Math.min(limit, 500));
        return jdbc.query(
                "SELECT event_id, tag_id, station_code, direction, occurred_at FROM wip_event ORDER BY occurred_at DESC, id DESC LIMIT ?",
                (rs, i) -> new WipEventView(
                        rs.getString("event_id"),
                        rs.getString("tag_id"),
                        rs.getString("station_code"),
                        rs.getString("direction"),
                        rs.getTimestamp("occurred_at").toInstant()
                ),
                safe
        );
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM wip_unit", Long.class);
        List<StationCount> byStation = jdbc.query(
                "SELECT last_station AS station_code, COUNT(*) AS qty FROM wip_unit GROUP BY last_station ORDER BY qty DESC",
                (rs, i) -> new StationCount(rs.getString("station_code"), rs.getLong("qty"))
        );
        return Map.of(
                "total", total == null ? 0 : total,
                "byStation", byStation
        );
    }
}
