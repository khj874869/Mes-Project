package com.mesproject.mescore.process;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class KpiService {

    private final ProcessRepository repo;
    private final JdbcTemplate jdbc;

    public KpiService(ProcessRepository repo, JdbcTemplate jdbc) {
        this.repo = repo;
        this.jdbc = jdbc;
    }

    public record StationKpi(
            String stationCode,
            String stationName,
            String lineCode,
            int seq,
            long wipQty,
            long outQty,
            double avgCycleSec,
            double p95CycleSec,
            int slaCycleSec,
            long openAlarms,
            long timeSavedSec,
            int bottleneckScore
    ) {}

    public record AssigneeKpi(
            String assignee,
            long assignedCount,
            long ackedCount,
            long closedCount,
            double avgMttaSec,
            double p95MttaSec,
            double avgMttrSec,
            double p95MttrSec
    ) {}

    public record AlarmStats(
            double avgMttaSec,
            double p95MttaSec,
            double avgMttrSec,
            double p95MttrSec,
            long ackedCount,
            long closedCount
    ) {}

    public record Overview(
            Instant from,
            Instant to,
            long outTotal,
            double avgCycleSec,
            double p95CycleSec,
            long openAlarms,
            long timeSavedSec,
            double efficiencyGainPct,
            AlarmStats alarmStats,
            List<AssigneeKpi> byAssignee,
            List<StationKpi> byStation
    ) {}

    public Overview overview(Instant from, Instant to) {
        if (from == null || to == null) {
            Instant now = Instant.now();
            to = now;
            from = now.minus(Duration.ofHours(24));
        }

        // Station meta
        Map<String, ProcessRepository.StationMeta> stationMeta = new HashMap<>();
        for (var m : repo.listStations()) {
            stationMeta.put(m.stationCode(), m);
        }

        List<ProcessRepository.CycleRow> cycles = repo.listCycleRows(from, to);

        // WIP 수량(현재) by station
        Map<String, Long> wipByStation = new HashMap<>();
        jdbc.query(
                "SELECT last_station AS station_code, COUNT(*) AS qty FROM wip_unit GROUP BY last_station",
                rs -> {
                    String s = rs.getString("station_code");
                    long q = rs.getLong("qty");
                    if (s != null) wipByStation.put(s, q);
                }
        );

        // group cycles by station
        Map<String, List<Double>> cycleByStation = new HashMap<>();
        for (var r : cycles) {
            cycleByStation.computeIfAbsent(r.stationCode(), k -> new ArrayList<>()).add(r.cycleSec());
        }

        List<ProcessRepository.StationSlaView> slas = repo.listSlaWithStationName();
        Map<String, ProcessRepository.StationSlaView> slaMap = new HashMap<>();
        for (var s : slas) slaMap.put(s.stationCode(), s);

        List<StationKpi> perStation = new ArrayList<>();
        long outTotal = 0;
        long timeSavedTotal = 0;
        long manualTotal = 0;

        List<Double> allCycles = new ArrayList<>();

        for (var entry : cycleByStation.entrySet()) {
            String stationCode = entry.getKey();
            List<Double> list = entry.getValue();
            if (list == null || list.isEmpty()) continue;

            allCycles.addAll(list);

            long outQty = list.size();
            outTotal += outQty;

            double avg = list.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double p95 = percentile(list, 0.95);

            var sla = slaMap.getOrDefault(
                    stationCode,
                    new ProcessRepository.StationSlaView(stationCode, 300, 600, 30, 5, 120, 1, Instant.now())
            );
            int slaCycle = sla.maxCycleSec();

            long openAlarms = repo.countOpenAlarmsByStation(stationCode);

            int manual = Math.max(0, sla.manualCheckSec());
            int auto = Math.max(0, sla.autoCheckSec());
            int savedPer = Math.max(0, manual - auto);
            long timeSavedSec = (long) savedPer * outQty;
            timeSavedTotal += timeSavedSec;
            manualTotal += (long) manual * outQty;

            long wipQty = wipByStation.getOrDefault(stationCode, 0L);

            double ratio = avg / Math.max(1, sla.idealCycleSec());
            int score = (int) Math.round(Math.min(100.0, Math.max(0.0, (ratio * 40.0) + (openAlarms * 15.0) + (wipQty * 3.0))));

            ProcessRepository.StationMeta meta = stationMeta.get(stationCode);
            String stationName = meta == null ? "" : meta.stationName();
            String lineCode = meta == null ? "" : meta.lineCode();
            int seq = meta == null ? 0 : meta.seq();

            perStation.add(new StationKpi(
                    stationCode,
                    stationName,
                    lineCode,
                    seq,
                    wipQty,
                    outQty,
                    round1(avg),
                    round1(p95),
                    slaCycle,
                    openAlarms,
                    timeSavedSec,
                    score
            ));
        }

        // 기본 정렬: 병목 점수 DESC
        perStation.sort(Comparator.comparingInt(StationKpi::bottleneckScore).reversed());

        double avgAll = allCycles.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double p95All = percentile(allCycles, 0.95);

        long openAlarmsAll = repo.countOpenAlarms();
        double efficiencyGainPct = manualTotal <= 0 ? 0.0 : (timeSavedTotal * 100.0 / manualTotal);

        // ===== Alarm MTTA/MTTR & Assignee performance =====
        List<ProcessRepository.AlarmDurationRow> durs = repo.listAlarmDurations(from, to);

        List<Double> mttaAll = new ArrayList<>();
        List<Double> mttrAll = new ArrayList<>();
        Map<String, List<Double>> mttaBy = new HashMap<>();
        Map<String, List<Double>> mttrBy = new HashMap<>();
        Map<String, long[]> counts = new HashMap<>(); // [assigned, acked, closed]

        for (var d : durs) {
            String assignee = (d.assignedTo() == null || d.assignedTo().isBlank()) ? "UNASSIGNED" : d.assignedTo();
            counts.computeIfAbsent(assignee, k -> new long[3])[0]++;

            if (d.mttaSec() != null && d.mttaSec() >= 0) {
                mttaAll.add(d.mttaSec());
                mttaBy.computeIfAbsent(assignee, k -> new ArrayList<>()).add(d.mttaSec());
                counts.get(assignee)[1]++;
            }
            if (d.mttrSec() != null && d.mttrSec() >= 0) {
                mttrAll.add(d.mttrSec());
                mttrBy.computeIfAbsent(assignee, k -> new ArrayList<>()).add(d.mttrSec());
                counts.get(assignee)[2]++;
            }
        }

        AlarmStats alarmStats = new AlarmStats(
                round1(avg(mttaAll)),
                round1(percentile(mttaAll, 0.95)),
                round1(avg(mttrAll)),
                round1(percentile(mttrAll, 0.95)),
                mttaAll.size(),
                mttrAll.size()
        );

        List<AssigneeKpi> byAssignee = new ArrayList<>();
        for (var e : counts.entrySet()) {
            String a = e.getKey();
            long assigned = e.getValue()[0];
            long acked = e.getValue()[1];
            long closed = e.getValue()[2];

            List<Double> mtta = mttaBy.getOrDefault(a, List.of());
            List<Double> mttr = mttrBy.getOrDefault(a, List.of());

            byAssignee.add(new AssigneeKpi(
                    a,
                    assigned,
                    acked,
                    closed,
                    round1(avg(mtta)),
                    round1(percentile(mtta, 0.95)),
                    round1(avg(mttr)),
                    round1(percentile(mttr, 0.95))
            ));
        }

        byAssignee.sort(Comparator
                .comparingLong(AssigneeKpi::closedCount).reversed()
                .thenComparingDouble(AssigneeKpi::avgMttrSec));

        return new Overview(
                from,
                to,
                outTotal,
                round1(avgAll),
                round1(p95All),
                openAlarmsAll,
                timeSavedTotal,
                round1(efficiencyGainPct),
                alarmStats,
                byAssignee,
                perStation
        );
    }

    private static double avg(List<Double> xs) {
        if (xs == null || xs.isEmpty()) return 0.0;
        return xs.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private static double percentile(List<Double> values, double p) {
        if (values == null || values.isEmpty()) return 0.0;
        List<Double> xs = new ArrayList<>(values);
        xs.sort(Double::compareTo);
        int n = xs.size();
        if (n == 1) return xs.get(0);
        double idx = p * (n - 1);
        int lo = (int) Math.floor(idx);
        int hi = (int) Math.ceil(idx);
        if (lo == hi) return xs.get(lo);
        double w = idx - lo;
        return xs.get(lo) * (1.0 - w) + xs.get(hi) * w;
    }
}
