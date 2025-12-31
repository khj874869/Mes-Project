package com.mesproject.mescore.ext.system;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.mesproject.mescore.ext.system.SystemDtos.LineView;
import static com.mesproject.mescore.ext.system.SystemDtos.StationView;

@Repository
public class SystemMasterRepository {

    private final JdbcTemplate jdbc;

    public SystemMasterRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<LineView> findLines(boolean onlyActive) {
        String sql = "SELECT line_code, name, active FROM mes_line" + (onlyActive ? " WHERE active=true" : "") + " ORDER BY line_code";
        return jdbc.query(sql, (rs, i) -> new LineView(
                rs.getString("line_code"),
                rs.getString("name"),
                rs.getBoolean("active")
        ));
    }

    public List<StationView> findStations(boolean onlyActive, boolean onlyKioskEnabled) {
        String sql = "SELECT station_code, name, line_code, seq, kiosk_enabled, active FROM mes_station WHERE 1=1";
        if (onlyActive) sql += " AND active=true";
        if (onlyKioskEnabled) sql += " AND kiosk_enabled=true";
        sql += " ORDER BY line_code, seq, station_code";
        return jdbc.query(sql, (rs, i) -> new StationView(
                rs.getString("station_code"),
                rs.getString("name"),
                rs.getString("line_code"),
                rs.getInt("seq"),
                rs.getBoolean("kiosk_enabled"),
                rs.getBoolean("active")
        ));
    }

    public void upsertLine(String lineCode, String name, boolean active) {
        jdbc.update(
                "INSERT INTO mes_line(line_code, name, active) VALUES (?,?,?) " +
                        "ON CONFLICT (line_code) DO UPDATE SET name=EXCLUDED.name, active=EXCLUDED.active",
                lineCode, name, active
        );
    }

    public void upsertStation(String stationCode, String name, String lineCode, int seq, boolean kioskEnabled, boolean active) {
        jdbc.update(
                "INSERT INTO mes_station(station_code, name, line_code, seq, kiosk_enabled, active) VALUES (?,?,?,?,?,?) " +
                        "ON CONFLICT (station_code) DO UPDATE SET " +
                        "name=EXCLUDED.name, line_code=EXCLUDED.line_code, seq=EXCLUDED.seq, kiosk_enabled=EXCLUDED.kiosk_enabled, active=EXCLUDED.active",
                stationCode, name, lineCode, seq, kioskEnabled, active
        );
    }
}
