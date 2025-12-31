package com.mesproject.mescore.process;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class ProcessRepository {

    private final JdbcTemplate jdbc;

    public ProcessRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record StationSlaView(
            String stationCode,
            int maxCycleSec,
            int maxStuckSec,
            int manualCheckSec,
            int autoCheckSec,
            int idealCycleSec,
            int manning,
            Instant updatedAt
    ) {}

    public record AlarmView(
            long id,
            String alarmType,
            String severity,
            String status,
            String tagId,
            String stationCode,
            String lineCode,
            String message,
            Instant occurredAt,
            Instant detectedAt,
            Instant ackedAt,
            Instant closedAt,
            String assignedTo,
            String lastUpdatedBy,
            Instant updatedAt
    ) {}

    public record AlarmHistoryView(
            long id,
            long alarmId,
            String action,
            String actor,
            String note,
            Instant createdAt
    ) {}

    
    public record StationMeta(String stationCode, String stationName, String lineCode, int seq) {}
    public record AlarmDurationRow(String assignedTo, Double mttaSec, Double mttrSec) {}

public record UnitAtStation(String tagId, String stationCode) {}

    public Optional<String> findLineCodeByStation(String stationCode) {
        List<String> xs = jdbc.query(
                "SELECT line_code FROM mes_station WHERE station_code=?",
                (rs, i) -> rs.getString("line_code"),
                stationCode
        );
        return xs.isEmpty() ? Optional.empty() : Optional.ofNullable(xs.get(0));
    }

    public List<StationMeta> listStations() {
        return jdbc.query(
                "SELECT station_code, name, line_code, seq FROM mes_station ORDER BY line_code, seq, station_code",
                (rs, i) -> new StationMeta(
                        rs.getString("station_code"),
                        rs.getString("name"),
                        rs.getString("line_code"),
                        rs.getInt("seq")
                )
        );
    }


    public Optional<StationSlaView> findSla(String stationCode) {
        List<StationSlaView> xs = jdbc.query(
                "SELECT station_code, max_cycle_sec, max_stuck_sec, manual_check_sec, auto_check_sec, ideal_cycle_sec, manning, updated_at " +
                        "FROM station_sla WHERE station_code=?",
                (rs, i) -> new StationSlaView(
                        rs.getString("station_code"),
                        rs.getInt("max_cycle_sec"),
                        rs.getInt("max_stuck_sec"),
                        rs.getInt("manual_check_sec"),
                        rs.getInt("auto_check_sec"),
                        rs.getInt("ideal_cycle_sec"),
                        rs.getInt("manning"),
                        rs.getTimestamp("updated_at").toInstant()
                ),
                stationCode
        );
        return xs.isEmpty() ? Optional.empty() : Optional.of(xs.get(0));
    }

    public List<StationSlaView> listSlaWithStationName() {
        return jdbc.query(
                "SELECT s.station_code, sla.max_cycle_sec, sla.max_stuck_sec, sla.manual_check_sec, sla.auto_check_sec, sla.ideal_cycle_sec, sla.manning, sla.updated_at " +
                        "FROM mes_station s " +
                        "LEFT JOIN station_sla sla ON sla.station_code=s.station_code " +
                        "ORDER BY s.line_code, s.seq, s.station_code",
                (rs, i) -> new StationSlaView(
                        rs.getString("station_code"),
                        rs.getInt("max_cycle_sec"),
                        rs.getInt("max_stuck_sec"),
                        rs.getInt("manual_check_sec"),
                        rs.getInt("auto_check_sec"),
                        rs.getInt("ideal_cycle_sec"),
                        rs.getInt("manning"),
                        rs.getTimestamp("updated_at") == null ? Instant.now() : rs.getTimestamp("updated_at").toInstant()
                )
        );
    }

    public void upsertSla(StationSlaView sla) {
        jdbc.update(
                "INSERT INTO station_sla(station_code, max_cycle_sec, max_stuck_sec, manual_check_sec, auto_check_sec, ideal_cycle_sec, manning, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?, now()) " +
                        "ON CONFLICT (station_code) DO UPDATE SET " +
                        "max_cycle_sec=EXCLUDED.max_cycle_sec, " +
                        "max_stuck_sec=EXCLUDED.max_stuck_sec, " +
                        "manual_check_sec=EXCLUDED.manual_check_sec, " +
                        "auto_check_sec=EXCLUDED.auto_check_sec, " +
                        "ideal_cycle_sec=EXCLUDED.ideal_cycle_sec, " +
                        "manning=EXCLUDED.manning, " +
                        "updated_at=now()",
                sla.stationCode,
                sla.maxCycleSec,
                sla.maxStuckSec,
                sla.manualCheckSec,
                sla.autoCheckSec,
                sla.idealCycleSec,
                sla.manning
        );
    }

    /**
     * OUT 이벤트 기준 cycle(sec)을 한 번의 SQL로 계산
     */
    public record CycleRow(String tagId, String stationCode, Instant outAt, Instant inAt, double cycleSec) {}

    public List<CycleRow> listCycleRows(Instant from, Instant to) {
        return jdbc.query(
                "SELECT o.tag_id, o.station_code, o.occurred_at AS out_at, i.occurred_at AS in_at, " +
                        "EXTRACT(EPOCH FROM (o.occurred_at - i.occurred_at)) AS cycle_sec " +
                        "FROM wip_event o " +
                        "JOIN LATERAL (" +
                        "  SELECT occurred_at FROM wip_event " +
                        "  WHERE tag_id=o.tag_id AND station_code=o.station_code AND direction='IN' AND occurred_at <= o.occurred_at " +
                        "  ORDER BY occurred_at DESC, id DESC LIMIT 1" +
                        ") i ON TRUE " +
                        "WHERE o.direction='OUT' AND o.occurred_at BETWEEN ? AND ?",
                (rs, i) -> new CycleRow(
                        rs.getString("tag_id"),
                        rs.getString("station_code"),
                        rs.getTimestamp("out_at").toInstant(),
                        rs.getTimestamp("in_at").toInstant(),
                        rs.getDouble("cycle_sec")
                ),
                Timestamp.from(from),
                Timestamp.from(to)
        );
    }

    public Optional<Double> findCycleSecForOut(String tagId, String stationCode, Instant outAt) {
        List<Double> xs = jdbc.query(
                "SELECT EXTRACT(EPOCH FROM (?::timestamptz - i.occurred_at)) AS cycle_sec " +
                        "FROM (" +
                        "  SELECT occurred_at FROM wip_event " +
                        "  WHERE tag_id=? AND station_code=? AND direction='IN' AND occurred_at <= ? " +
                        "  ORDER BY occurred_at DESC, id DESC LIMIT 1" +
                        ") i",
                (rs, i) -> rs.getDouble("cycle_sec"),
                Timestamp.from(outAt),
                tagId,
                stationCode,
                Timestamp.from(outAt)
        );
        return xs.isEmpty() ? Optional.empty() : Optional.of(xs.get(0));
    }

    public List<UnitAtStation> listUnitsWithStation(int limit) {
        int safe = Math.max(1, Math.min(limit, 5000));
        return jdbc.query(
                "SELECT tag_id, last_station FROM wip_unit WHERE last_station IS NOT NULL ORDER BY updated_at DESC LIMIT ?",
                (rs, i) -> new UnitAtStation(rs.getString("tag_id"), rs.getString("last_station")),
                safe
        );
    }

    public Optional<Instant> findLastInAt(String tagId, String stationCode) {
        List<Instant> xs = jdbc.query(
                "SELECT occurred_at FROM wip_event WHERE tag_id=? AND station_code=? AND direction='IN' ORDER BY occurred_at DESC, id DESC LIMIT 1",
                (rs, i) -> rs.getTimestamp("occurred_at").toInstant(),
                tagId,
                stationCode
        );
        return xs.isEmpty() ? Optional.empty() : Optional.of(xs.get(0));
    }

    public boolean existsOutAfter(String tagId, String stationCode, Instant after) {
        Integer v = jdbc.query(
                "SELECT 1 FROM wip_event WHERE tag_id=? AND station_code=? AND direction='OUT' AND occurred_at > ? LIMIT 1",
                rs -> rs.next() ? 1 : null,
                tagId,
                stationCode,
                Timestamp.from(after)
        );
        return v != null;
    }

    public Optional<AlarmView> insertAlarmIfAbsent(
            String alarmType,
            String severity,
            String status,
            String tagId,
            String stationCode,
            String message,
            Instant occurredAt,
            String actor
    ) {
        String lineCode = findLineCodeByStation(stationCode).orElse(null);
        // 부분 유니크 인덱스(OPEN/ACK) 덕분에 중복 OPEN 알람은 막힘
        jdbc.update(
                "INSERT INTO alarm_event(alarm_type, severity, status, tag_id, station_code, line_code, message, occurred_at, detected_at, last_updated_by, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?, now(), ?, now()) " +
                        "ON CONFLICT (alarm_type, tag_id, station_code) DO NOTHING",
                alarmType,
                severity,
                status,
                tagId,
                stationCode,
                lineCode,
                message,
                occurredAt == null ? null : Timestamp.from(occurredAt),
                actor
        );

        List<AlarmView> xs = jdbc.query(
                "SELECT * FROM alarm_event WHERE alarm_type=? AND tag_id=? AND station_code=? AND status IN ('OPEN','ACK') ORDER BY id DESC LIMIT 1",
                (rs, i) -> mapAlarm(rs),
                alarmType, tagId, stationCode
        );

        if (xs.isEmpty()) return Optional.empty();
        AlarmView a = xs.get(0);

        // 최초 생성일 때만 CREATED history 남기기 (이미 존재하는 경우 history가 누적되지 않도록)
        Integer existsCreated = jdbc.query(
                "SELECT 1 FROM alarm_history WHERE alarm_id=? AND action='CREATED' LIMIT 1",
                rs -> rs.next() ? 1 : null,
                a.id
        );
        if (existsCreated == null) {
            insertHistory(a.id, "CREATED", actor, a.message);
        }
        return Optional.of(a);
    }

    public void insertHistory(long alarmId, String action, String actor, String note) {
        jdbc.update(
                "INSERT INTO alarm_history(alarm_id, action, actor, note) VALUES (?,?,?,?)",
                alarmId,
                action,
                actor,
                note
        );
    }

    public List<AlarmView> listAlarms(String status, int limit) {
        int safe = Math.max(1, Math.min(limit, 1000));
        String sql = "SELECT * FROM alarm_event";
        if (status != null && !status.isBlank()) {
            sql += " WHERE status=?";
            sql += " ORDER BY detected_at DESC, id DESC LIMIT ?";
            return jdbc.query(sql, (rs, i) -> mapAlarm(rs), status.toUpperCase(), safe);
        }
        sql += " ORDER BY detected_at DESC, id DESC LIMIT ?";
        return jdbc.query(sql, (rs, i) -> mapAlarm(rs), safe);
    }

    public List<AlarmView> listAlarmsBetween(Instant from, Instant to, int limit) {
        int safe = Math.max(1, Math.min(limit, 5000));
        return jdbc.query(
                "SELECT * FROM alarm_event WHERE detected_at BETWEEN ? AND ? ORDER BY detected_at DESC, id DESC LIMIT ?",
                (rs, i) -> mapAlarm(rs),
                Timestamp.from(from),
                Timestamp.from(to),
                safe
        );
    }

    public List<AlarmDurationRow> listAlarmDurations(Instant from, Instant to) {
        return jdbc.query(
                "SELECT assigned_to, " +
                        "CASE WHEN acked_at IS NULL THEN NULL ELSE EXTRACT(EPOCH FROM (acked_at - detected_at)) END AS mtta_sec, " +
                        "CASE WHEN closed_at IS NULL THEN NULL ELSE EXTRACT(EPOCH FROM (closed_at - detected_at)) END AS mttr_sec " +
                        "FROM alarm_event WHERE detected_at BETWEEN ? AND ?",
                (rs, i) -> new AlarmDurationRow(
                        rs.getString("assigned_to"),
                        rs.getObject("mtta_sec") == null ? null : rs.getDouble("mtta_sec"),
                        rs.getObject("mttr_sec") == null ? null : rs.getDouble("mttr_sec")
                ),
                Timestamp.from(from),
                Timestamp.from(to)
        );
    }
    public Optional<AlarmView> findAlarm(long id) {
        List<AlarmView> xs = jdbc.query(
                "SELECT * FROM alarm_event WHERE id=?",
                (rs, i) -> mapAlarm(rs),
                id
        );
        return xs.isEmpty() ? Optional.empty() : Optional.of(xs.get(0));
    }

    public List<AlarmHistoryView> listHistory(long alarmId) {
        return jdbc.query(
                "SELECT * FROM alarm_history WHERE alarm_id=? ORDER BY created_at DESC, id DESC LIMIT 200",
                (rs, i) -> new AlarmHistoryView(
                        rs.getLong("id"),
                        rs.getLong("alarm_id"),
                        rs.getString("action"),
                        rs.getString("actor"),
                        rs.getString("note"),
                        rs.getTimestamp("created_at").toInstant()
                ),
                alarmId
        );
    }

    public boolean ackAlarm(long id, String actor) {
        int n = jdbc.update(
                "UPDATE alarm_event SET status='ACK', acked_at=COALESCE(acked_at, now()), last_updated_by=?, updated_at=now() " +
                        "WHERE id=? AND status='OPEN'",
                actor,
                id
        );
        if (n > 0) insertHistory(id, "ACK", actor, null);
        return n > 0;
    }

    public boolean assignAlarm(long id, String assignee, String actor) {
        int n = jdbc.update(
                "UPDATE alarm_event SET assigned_to=?, last_updated_by=?, updated_at=now() WHERE id=? AND status IN ('OPEN','ACK')",
                assignee,
                actor,
                id
        );
        if (n > 0) insertHistory(id, "ASSIGN", actor, assignee);
        return n > 0;
    }

    public boolean closeAlarm(long id, String actor, String note) {
        int n = jdbc.update(
                "UPDATE alarm_event SET status='CLOSED', closed_at=COALESCE(closed_at, now()), last_updated_by=?, updated_at=now() " +
                        "WHERE id=? AND status IN ('OPEN','ACK')",
                actor,
                id
        );
        if (n > 0) insertHistory(id, "CLOSE", actor, note);
        return n > 0;
    }

    public long countOpenAlarms() {
        Long v = jdbc.queryForObject("SELECT COUNT(*) FROM alarm_event WHERE status IN ('OPEN','ACK')", Long.class);
        return v == null ? 0 : v;
    }

    public long countOpenAlarmsByStation(String stationCode) {
        Long v = jdbc.queryForObject(
                "SELECT COUNT(*) FROM alarm_event WHERE status IN ('OPEN','ACK') AND station_code=?",
                Long.class,
                stationCode
        );
        return v == null ? 0 : v;
    }

    private static AlarmView mapAlarm(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new AlarmView(
                rs.getLong("id"),
                rs.getString("alarm_type"),
                rs.getString("severity"),
                rs.getString("status"),
                rs.getString("tag_id"),
                rs.getString("station_code"),
                rs.getString("line_code"),
                rs.getString("message"),
                rs.getTimestamp("occurred_at") == null ? null : rs.getTimestamp("occurred_at").toInstant(),
                rs.getTimestamp("detected_at") == null ? null : rs.getTimestamp("detected_at").toInstant(),
                rs.getTimestamp("acked_at") == null ? null : rs.getTimestamp("acked_at").toInstant(),
                rs.getTimestamp("closed_at") == null ? null : rs.getTimestamp("closed_at").toInstant(),
                rs.getString("assigned_to"),
                rs.getString("last_updated_by"),
                rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toInstant()
        );
    }
}
