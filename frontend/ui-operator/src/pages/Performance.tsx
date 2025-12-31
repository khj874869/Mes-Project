import { useEffect, useMemo, useState } from "react";
import { CORE } from "../lib/api";
import { getJson, getToken } from "../lib/http";

type StationKpi = {
  stationCode: string;
  stationName: string;
  lineCode: string;
  seq: number;
  wipQty: number;
  outQty: number;
  avgCycleSec: number;
  p95CycleSec: number;
  slaCycleSec: number;
  openAlarms: number;
  timeSavedSec: number;
  bottleneckScore: number;
};

type AlarmStats = {
  avgMttaSec: number;
  p95MttaSec: number;
  avgMttrSec: number;
  p95MttrSec: number;
  ackedCount: number;
  closedCount: number;
};

type AssigneeKpi = {
  assignee: string;
  assignedCount: number;
  ackedCount: number;
  closedCount: number;
  avgMttaSec: number;
  p95MttaSec: number;
  avgMttrSec: number;
  p95MttrSec: number;
};

type Overview = {
  from: string;
  to: string;
  outTotal: number;
  avgCycleSec: number;
  p95CycleSec: number;
  openAlarms: number;
  timeSavedSec: number;
  efficiencyGainPct: number;
  alarmStats: AlarmStats;
  byAssignee: AssigneeKpi[];
  byStation: StationKpi[];
};

function iso(dt: Date) {
  return dt.toISOString();
}

function fmtSec(sec: number) {
  if (!isFinite(sec)) return "0";
  if (sec < 60) return `${sec.toFixed(0)}s`;
  const m = Math.floor(sec / 60);
  const s = Math.floor(sec % 60);
  return `${m}m ${s}s`;
}

function heatColor(score: number) {
  // 0=green, 100=red
  const s = Math.max(0, Math.min(100, score));
  const hue = (100 - s) * 1.2; // 0..120
  return `hsl(${hue.toFixed(0)}, 85%, 55%)`;
}

export default function Performance() {
  const now = useMemo(() => new Date(), []);
  const [from, setFrom] = useState(iso(new Date(now.getTime() - 24 * 3600 * 1000)));
  const [to, setTo] = useState(iso(now));
  const [ov, setOv] = useState<Overview | null>(null);
  const [err, setErr] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const load = async () => {
    setBusy(true);
    try {
      const data = await getJson<Overview>(`${CORE}/kpi/overview?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`);
      setOv(data);
      setErr(null);
    } catch (e: any) {
      setErr(e?.message ?? String(e));
    } finally {
      setBusy(false);
    }
  };

  useEffect(() => { load(); }, []);

  const downloadExcel = async () => {
    const token = getToken();
    if (!token) {
      setErr("로그인이 필요합니다.");
      return;
    }
    setBusy(true);
    try {
      const res = await fetch(`${CORE}/kpi/report.xlsx?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`, {
        headers: { "Authorization": `Bearer ${token}` }
      });
      if (!res.ok) throw new Error(`download failed: ${res.status}`);
      const blob = await res.blob();
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `mes_kpi_${from.replace(/[:]/g, "-")}_${to.replace(/[:]/g, "-")}.xlsx`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
    } catch (e: any) {
      setErr(e?.message ?? String(e));
    } finally {
      setBusy(false);
    }
  };

  const heat = useMemo(() => {
    if (!ov) return [];
    const m = new Map<string, StationKpi[]>();
    for (const s of ov.byStation || []) {
      const line = s.lineCode || "LINE";
      const xs = m.get(line) ?? [];
      xs.push(s);
      m.set(line, xs);
    }
    const lines = Array.from(m.entries()).map(([line, xs]) => ({
      line,
      stations: xs.slice().sort((a, b) => (a.seq ?? 0) - (b.seq ?? 0))
    }));
    lines.sort((a, b) => a.line.localeCompare(b.line));
    return lines;
  }, [ov]);

  return (
    <div className="grid">
      <div className="card">
        <div className="row" style={{ justifyContent: "space-between" }}>
          <div>
            <h1 className="h1">Performance KPI</h1>
            <div className="muted">시간 단축/효율 개선 + 알람 대응 성과(MTTA/MTTR) + 병목 Heatmap + Excel 다운로드</div>
          </div>
          <div className="row">
            <button className="btn secondary" disabled={busy} onClick={downloadExcel}>Excel 다운로드</button>
            <button className="btn" disabled={busy} onClick={load}>{busy ? "로딩..." : "조회"}</button>
          </div>
        </div>

        <div className="row" style={{ marginTop: 12, gap: 12, flexWrap: "wrap" }}>
          <label className="row">
            <span className="muted">From</span>
            <input className="input" style={{ width: 260 }} value={from} onChange={(e) => setFrom(e.target.value)} />
          </label>
          <label className="row">
            <span className="muted">To</span>
            <input className="input" style={{ width: 260 }} value={to} onChange={(e) => setTo(e.target.value)} />
          </label>
        </div>

        {err && <div className="bad" style={{ marginTop: 10 }}>ERROR: {err}</div>}
      </div>

      {ov && (
        <>
          <div className="grid grid-4">
            <div className="card"><div className="muted">OUT Total</div><div className="h1" style={{ marginTop: 6 }}>{ov.outTotal}</div></div>
            <div className="card"><div className="muted">Avg Cycle</div><div className="h1" style={{ marginTop: 6 }}>{fmtSec(ov.avgCycleSec)}</div></div>
            <div className="card"><div className="muted">P95 Cycle</div><div className="h1" style={{ marginTop: 6 }}>{fmtSec(ov.p95CycleSec)}</div></div>
            <div className="card"><div className="muted">Open Alarms</div><div className="h1" style={{ marginTop: 6 }}>{ov.openAlarms}</div></div>
          </div>

          <div className="grid grid-3">
            <div className="card"><div className="muted">Time Saved</div><div className="h1" style={{ marginTop: 6 }}>{fmtSec(ov.timeSavedSec)}</div></div>
            <div className="card"><div className="muted">Efficiency Gain</div><div className="h1" style={{ marginTop: 6 }}>{ov.efficiencyGainPct.toFixed(1)}%</div></div>
            <div className="card"><div className="muted">Window</div><div className="h2" style={{ marginTop: 6 }}>{new Date(ov.from).toLocaleString()} → {new Date(ov.to).toLocaleString()}</div></div>
          </div>

          {ov.alarmStats && (
            <div className="grid grid-4">
              <div className="card"><div className="muted">Avg MTTA</div><div className="h1" style={{ marginTop: 6 }}>{fmtSec(ov.alarmStats.avgMttaSec)}</div><div className="muted">ACK {ov.alarmStats.ackedCount}</div></div>
              <div className="card"><div className="muted">P95 MTTA</div><div className="h1" style={{ marginTop: 6 }}>{fmtSec(ov.alarmStats.p95MttaSec)}</div></div>
              <div className="card"><div className="muted">Avg MTTR</div><div className="h1" style={{ marginTop: 6 }}>{fmtSec(ov.alarmStats.avgMttrSec)}</div><div className="muted">CLOSE {ov.alarmStats.closedCount}</div></div>
              <div className="card"><div className="muted">P95 MTTR</div><div className="h1" style={{ marginTop: 6 }}>{fmtSec(ov.alarmStats.p95MttrSec)}</div></div>
            </div>
          )}

          <div className="card">
            <div className="h2">Bottleneck Heatmap (Line/Station)</div>
            <div className="muted">점수가 높을수록 병목(빨강). cycle/ideal + alarms + wip 기반.</div>

            <div style={{ marginTop: 12, display: "grid", gap: 14 }}>
              {heat.map((l) => (
                <div key={l.line}>
                  <div className="row" style={{ justifyContent: "space-between" }}>
                    <div className="h2">{l.line}</div>
                    <div className="muted">{l.stations.length} stations</div>
                  </div>
                  <div style={{ display: "flex", gap: 10, flexWrap: "wrap", marginTop: 10 }}>
                    {l.stations.map((s) => {
                      const bg = heatColor(s.bottleneckScore);
                      const title = `${s.lineCode} / ${s.stationCode} ${s.stationName || ""}\nWIP=${s.wipQty} OUT=${s.outQty}\nAvg=${s.avgCycleSec}s P95=${s.p95CycleSec}s\nOpenAlarms=${s.openAlarms}`;
                      return (
                        <div
                          key={s.stationCode}
                          title={title}
                          style={{
                            width: 140,
                            minHeight: 78,
                            padding: 10,
                            borderRadius: 14,
                            background: bg,
                            border: "1px solid rgba(0,0,0,0.14)",
                            boxShadow: "0 2px 12px rgba(0,0,0,0.10)"
                          }}
                        >
                          <div className="row" style={{ justifyContent: "space-between" }}>
                            <code style={{ fontWeight: 700 }}>{s.stationCode}</code>
                            <span className="badge">{s.bottleneckScore}</span>
                          </div>
                          <div style={{ marginTop: 6, fontSize: 13, fontWeight: 600 }}>{s.stationName || "-"}</div>
                          <div className="muted" style={{ marginTop: 6, fontSize: 12 }}>WIP {s.wipQty} · OUT {s.outQty}</div>
                        </div>
                      );
                    })}
                    {!l.stations.length && <div className="muted">데이터 없음</div>}
                  </div>
                </div>
              ))}
              {!heat.length && <div className="muted">Heatmap 데이터가 없습니다. (OUT 이벤트가 필요합니다)</div>}
            </div>
          </div>

          <div className="card">
            <div className="h2">Assignee Performance (Alarm MTTA/MTTR)</div>
            <div className="muted">담당자별 ACK/조치 성과를 MTTA/MTTR로 집계합니다.</div>

            <div style={{ overflowX: "auto", marginTop: 10 }}>
              <table className="table">
                <thead>
                  <tr>
                    <th>Assignee</th>
                    <th>Assigned</th>
                    <th>Acked</th>
                    <th>Closed</th>
                    <th>Avg MTTA</th>
                    <th>P95 MTTA</th>
                    <th>Avg MTTR</th>
                    <th>P95 MTTR</th>
                  </tr>
                </thead>
                <tbody>
                  {(ov.byAssignee || []).map((a) => (
                    <tr key={a.assignee}>
                      <td><code>{a.assignee}</code></td>
                      <td>{a.assignedCount}</td>
                      <td>{a.ackedCount}</td>
                      <td>{a.closedCount}</td>
                      <td>{fmtSec(a.avgMttaSec)}</td>
                      <td>{fmtSec(a.p95MttaSec)}</td>
                      <td>{fmtSec(a.avgMttrSec)}</td>
                      <td>{fmtSec(a.p95MttrSec)}</td>
                    </tr>
                  ))}
                  {!ov.byAssignee?.length && (
                    <tr><td colSpan={8} className="muted">알람 데이터가 없습니다.</td></tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>

          <div className="card">
            <div className="row" style={{ justifyContent: "space-between" }}>
              <div className="h2">Station KPI (bottleneck 순)</div>
              <div className="muted">Excel에는 Summary/Station Metrics/Assignee Performance/Alarms(Window) 시트가 포함됩니다.</div>
            </div>

            <div style={{ overflowX: "auto", marginTop: 10 }}>
              <table className="table">
                <thead>
                  <tr>
                    <th>Line</th>
                    <th>Seq</th>
                    <th>Station</th>
                    <th>Name</th>
                    <th>WIP</th>
                    <th>OUT</th>
                    <th>Avg</th>
                    <th>P95</th>
                    <th>SLA</th>
                    <th>OpenAlarms</th>
                    <th>TimeSaved</th>
                    <th>Bottleneck</th>
                  </tr>
                </thead>
                <tbody>
                  {ov.byStation.map((s) => (
                    <tr key={s.stationCode}>
                      <td><code>{s.lineCode || "-"}</code></td>
                      <td>{s.seq || "-"}</td>
                      <td><code>{s.stationCode}</code></td>
                      <td>{s.stationName || "-"}</td>
                      <td>{s.wipQty}</td>
                      <td>{s.outQty}</td>
                      <td>{fmtSec(s.avgCycleSec)}</td>
                      <td>{fmtSec(s.p95CycleSec)}</td>
                      <td>{fmtSec(s.slaCycleSec)}</td>
                      <td>{s.openAlarms}</td>
                      <td>{fmtSec(s.timeSavedSec)}</td>
                      <td>
                        <span className={s.bottleneckScore >= 70 ? "badge bad" : (s.bottleneckScore >= 40 ? "badge" : "badge ok")}>
                          {s.bottleneckScore}
                        </span>
                      </td>
                    </tr>
                  ))}
                  {!ov.byStation.length && (
                    <tr><td colSpan={12} className="muted">데이터가 없습니다. (OUT 이벤트가 필요합니다)</td></tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>

          <div className="card">
            <div className="muted">
              * 시간 절감(Time Saved) 산식: <code>(manual_check_sec - auto_check_sec) × OUT</code><br />
              * MTTA: Detect → ACK, MTTR: Detect → Close (초 단위)<br />
              * 외부 공개용 HTTPS는 Caddy(Reverse Proxy + 자동 인증서)로 구성됩니다.
            </div>
          </div>
        </>
      )}
    </div>
  );
}
