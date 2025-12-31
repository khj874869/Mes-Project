package com.mesproject.mescore.process;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Component
public class KpiExcelExporter {

    public byte[] export(KpiService.Overview ov, List<ProcessRepository.AlarmView> alarmsInWindow) {
        try (Workbook wb = new XSSFWorkbook()) {
            CreationHelper ch = wb.getCreationHelper();

            CellStyle headerStyle = wb.createCellStyle();
            Font hf = wb.createFont();
            hf.setBold(true);
            headerStyle.setFont(hf);

            CellStyle numberStyle = wb.createCellStyle();
            numberStyle.setDataFormat(ch.createDataFormat().getFormat("0.0"));

            // ===== Summary =====
            Sheet summary = wb.createSheet("Summary");
            int r = 0;
            r = kv(summary, r, "From", ov.from().toString(), headerStyle);
            r = kv(summary, r, "To", ov.to().toString(), headerStyle);
            r = kv(summary, r, "OUT Total", String.valueOf(ov.outTotal()), headerStyle);
            r = kv(summary, r, "Avg Cycle (sec)", String.valueOf(ov.avgCycleSec()), headerStyle);
            r = kv(summary, r, "P95 Cycle (sec)", String.valueOf(ov.p95CycleSec()), headerStyle);
            r = kv(summary, r, "Open Alarms", String.valueOf(ov.openAlarms()), headerStyle);
            r = kv(summary, r, "Time Saved (sec)", String.valueOf(ov.timeSavedSec()), headerStyle);
            r = kv(summary, r, "Efficiency Gain (%)", String.valueOf(ov.efficiencyGainPct()), headerStyle);

            if (ov.alarmStats() != null) {
                r = kv(summary, r, "Avg MTTA (sec)", String.valueOf(ov.alarmStats().avgMttaSec()), headerStyle);
                r = kv(summary, r, "P95 MTTA (sec)", String.valueOf(ov.alarmStats().p95MttaSec()), headerStyle);
                r = kv(summary, r, "Avg MTTR (sec)", String.valueOf(ov.alarmStats().avgMttrSec()), headerStyle);
                r = kv(summary, r, "P95 MTTR (sec)", String.valueOf(ov.alarmStats().p95MttrSec()), headerStyle);
                r = kv(summary, r, "Acked Count", String.valueOf(ov.alarmStats().ackedCount()), headerStyle);
                r = kv(summary, r, "Closed Count", String.valueOf(ov.alarmStats().closedCount()), headerStyle);
            }

            summary.autoSizeColumn(0);
            summary.autoSizeColumn(1);

            // ===== Station metrics =====
            Sheet st = wb.createSheet("Station Metrics");
            Row h = st.createRow(0);
            String[] cols = {"Line", "Seq", "Station", "Name", "WIP", "OUT", "AvgCycleSec", "P95CycleSec", "SLA_CycleSec", "OpenAlarms", "TimeSavedSec", "BottleneckScore"};
            for (int i = 0; i < cols.length; i++) {
                Cell c = h.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(headerStyle);
            }

            int rr = 1;
            for (var x : ov.byStation()) {
                Row row = st.createRow(rr++);
                row.createCell(0).setCellValue(x.lineCode());
                row.createCell(1).setCellValue(x.seq());
                row.createCell(2).setCellValue(x.stationCode());
                row.createCell(3).setCellValue(x.stationName() == null ? "" : x.stationName());
                row.createCell(4).setCellValue(x.wipQty());
                row.createCell(5).setCellValue(x.outQty());

                Cell c6 = row.createCell(6); c6.setCellValue(x.avgCycleSec()); c6.setCellStyle(numberStyle);
                Cell c7 = row.createCell(7); c7.setCellValue(x.p95CycleSec()); c7.setCellStyle(numberStyle);

                row.createCell(8).setCellValue(x.slaCycleSec());
                row.createCell(9).setCellValue(x.openAlarms());
                row.createCell(10).setCellValue(x.timeSavedSec());
                row.createCell(11).setCellValue(x.bottleneckScore());
            }
            for (int i = 0; i < cols.length; i++) st.autoSizeColumn(i);

            // ===== Alarm stats by assignee =====
            Sheet ass = wb.createSheet("Assignee Performance");
            Row ah = ass.createRow(0);
            String[] aCols = {"Assignee", "Assigned", "Acked", "Closed", "AvgMTTA", "P95MTTA", "AvgMTTR", "P95MTTR"};
            for (int i = 0; i < aCols.length; i++) {
                Cell c = ah.createCell(i);
                c.setCellValue(aCols[i]);
                c.setCellStyle(headerStyle);
            }

            int ar = 1;
            if (ov.byAssignee() != null) {
                for (var a : ov.byAssignee()) {
                    Row row = ass.createRow(ar++);
                    row.createCell(0).setCellValue(a.assignee());
                    row.createCell(1).setCellValue(a.assignedCount());
                    row.createCell(2).setCellValue(a.ackedCount());
                    row.createCell(3).setCellValue(a.closedCount());

                    Cell c4 = row.createCell(4); c4.setCellValue(a.avgMttaSec()); c4.setCellStyle(numberStyle);
                    Cell c5 = row.createCell(5); c5.setCellValue(a.p95MttaSec()); c5.setCellStyle(numberStyle);
                    Cell c6 = row.createCell(6); c6.setCellValue(a.avgMttrSec()); c6.setCellStyle(numberStyle);
                    Cell c7 = row.createCell(7); c7.setCellValue(a.p95MttrSec()); c7.setCellStyle(numberStyle);
                }
            }
            for (int i = 0; i < aCols.length; i++) ass.autoSizeColumn(i);

            // ===== Alarms (Window) =====
            Sheet alarms = wb.createSheet("Alarms (Window)");
            Row alh = alarms.createRow(0);
            String[] alCols = {"Id", "Type", "Severity", "Status", "Tag", "Station", "Line", "Message", "DetectedAt", "AckedAt", "ClosedAt", "AssignedTo", "MTTA(sec)", "MTTR(sec)", "LastUpdatedBy"};
            for (int i = 0; i < alCols.length; i++) {
                Cell c = alh.createCell(i);
                c.setCellValue(alCols[i]);
                c.setCellStyle(headerStyle);
            }

            int alr = 1;
            for (var a : alarmsInWindow) {
                Row row = alarms.createRow(alr++);
                row.createCell(0).setCellValue(a.id());
                row.createCell(1).setCellValue(a.alarmType());
                row.createCell(2).setCellValue(a.severity());
                row.createCell(3).setCellValue(a.status());
                row.createCell(4).setCellValue(a.tagId());
                row.createCell(5).setCellValue(a.stationCode());
                row.createCell(6).setCellValue(a.lineCode() == null ? "" : a.lineCode());
                row.createCell(7).setCellValue(a.message() == null ? "" : a.message());
                row.createCell(8).setCellValue(a.detectedAt() == null ? "" : a.detectedAt().toString());
                row.createCell(9).setCellValue(a.ackedAt() == null ? "" : a.ackedAt().toString());
                row.createCell(10).setCellValue(a.closedAt() == null ? "" : a.closedAt().toString());
                row.createCell(11).setCellValue(a.assignedTo() == null ? "" : a.assignedTo());

                Double mtta = (a.ackedAt() == null || a.detectedAt() == null) ? null : (double) (a.ackedAt().getEpochSecond() - a.detectedAt().getEpochSecond());
                Double mttr = (a.closedAt() == null || a.detectedAt() == null) ? null : (double) (a.closedAt().getEpochSecond() - a.detectedAt().getEpochSecond());
                Cell c12 = row.createCell(12); c12.setCellValue(mtta == null ? 0.0 : mtta); c12.setCellStyle(numberStyle);
                Cell c13 = row.createCell(13); c13.setCellValue(mttr == null ? 0.0 : mttr); c13.setCellStyle(numberStyle);

                row.createCell(14).setCellValue(a.lastUpdatedBy() == null ? "" : a.lastUpdatedBy());
            }
            for (int i = 0; i < alCols.length; i++) alarms.autoSizeColumn(i);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static int kv(Sheet s, int r, String k, String v, CellStyle keyStyle) {
        Row row = s.createRow(r);
        Cell ck = row.createCell(0);
        ck.setCellValue(k);
        ck.setCellStyle(keyStyle);
        row.createCell(1).setCellValue(v == null ? "" : v);
        return r + 1;
    }
}
