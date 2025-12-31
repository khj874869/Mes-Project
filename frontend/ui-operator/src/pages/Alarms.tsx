import { useEffect, useMemo, useState } from "react";
import { CORE } from "../lib/api";
import { getJson, postJson } from "../lib/http";
import { withAccessToken } from "../lib/sse";

type Alarm = {
  id: number;
  alarmType: string;
  severity: "WARN" | "CRIT" | string;
  status: "OPEN" | "ACK" | "CLOSED" | string;
  tagId: string;
  stationCode: string;
  lineCode?: string | null;
  message?: string | null;
  occurredAt?: string | null;
  detectedAt?: string | null;
  assignedTo?: string | null;
  lastUpdatedBy?: string | null;
};

export default function Alarms() {
  const [status, setStatus] = useState("ALL");
  const [alarms, setAlarms] = useState<Alarm[]>([]);
  const [assignMap, setAssignMap] = useState<Record<number, string>>({});
  const [err, setErr] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<number | null>(null);

  const fetchAlarms = async () => {
    try {
      const qs = status === "ALL" ? "" : `?status=${encodeURIComponent(status)}`;
      const path = `${CORE}/alarms${qs}`;
      const data = await getJson<Alarm[]>(path + (path.includes("?") ? "&" : "?") + "limit=400");
      setAlarms(data);
      setAssignMap((prev) => {
        const next = { ...prev };
        data.forEach((a) => {
          if (!(a.id in next)) next[a.id] = a.assignedTo ?? "";
        });
        return next;
      });
      setErr(null);
    } catch (e: any) {
      setErr(e?.message ?? String(e));
    }
  };

  useEffect(() => { fetchAlarms(); }, [status]);

  // 주기적 새로고침
  useEffect(() => {
    const t = setInterval(() => fetchAlarms(), 10000);
    return () => clearInterval(t);
  }, [status]);

  // SSE로 알람 이벤트가 오면 즉시 갱신 (권한: USER/ADMIN 모두)
  useEffect(() => {
    const es = new EventSource(withAccessToken(`${CORE}/wip/stream`));
    const onAlarm = () => fetchAlarms();
    es.addEventListener("alarm", onAlarm as any);
    return () => {
      es.removeEventListener("alarm", onAlarm as any);
      es.close();
    };
  }, [status]);

  const ack = async (id: number) => {
    setBusyId(id);
    try {
      await postJson(`${CORE}/alarms/${id}/ack`, {});
      await fetchAlarms();
    } finally {
      setBusyId(null);
    }
  };

  const close = async (id: number) => {
    setBusyId(id);
    try {
      await postJson(`${CORE}/alarms/${id}/close`, { note: "resolved" });
      await fetchAlarms();
    } finally {
      setBusyId(null);
    }
  };

  const assign = async (id: number) => {
    setBusyId(id);
    try {
      await postJson(`${CORE}/alarms/${id}/assign`, { assignedTo: assignMap[id] ?? "" });
      await fetchAlarms();
    } finally {
      setBusyId(null);
    }
  };

  const counts = useMemo(() => {
    const c: Record<string, number> = { OPEN: 0, ACK: 0, CLOSED: 0 };
    alarms.forEach((a) => { c[a.status] = (c[a.status] ?? 0) + 1; });
    return c;
  }, [alarms]);

  return (
    <div className="grid">
      <div className="card">
        <div className="row" style={{ justifyContent: "space-between" }}>
          <div>
            <h1 className="h1">Alarm Center</h1>
            <div className="muted">CycleTime SLA 초과 · 공정 정체(STUCK) · 실시간 이벤트</div>
          </div>
          <button className="btn secondary" onClick={fetchAlarms}>새로고침</button>
        </div>
        <div className="row" style={{ marginTop: 10 }}>
          <span className="pill">OPEN {counts.OPEN ?? 0}</span>
          <span className="pill muted">ACK {counts.ACK ?? 0}</span>
          <span className="pill muted">CLOSED {counts.CLOSED ?? 0}</span>
        </div>
        {err && <div className="bad" style={{ marginTop: 10 }}>ERROR: {err}</div>}
      </div>

      <div className="card">
        <div className="row" style={{ justifyContent: "space-between" }}>
          <div className="row">
            <span className="muted">Status</span>
            <select className="input" style={{ width: 180 }} value={status} onChange={(e) => setStatus(e.target.value)}>
              <option value="ALL">ALL</option>
              <option value="OPEN">OPEN</option>
              <option value="ACK">ACK</option>
              <option value="CLOSED">CLOSED</option>
            </select>
          </div>
          <div className="muted">최근 {alarms.length}건</div>
        </div>

        <div style={{ overflowX: "auto", marginTop: 10 }}>
          <table className="table">
            <thead>
              <tr>
                <th>Detected</th>
                <th>Type</th>
                <th>Severity</th>
                <th>Status</th>
                <th>Tag</th>
                <th>Station</th>
                <th>Message</th>
                <th>Assigned</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {alarms.map((a) => (
                <tr key={a.id}>
                  <td className="muted" style={{ whiteSpace: "nowrap" }}>
                    {a.detectedAt ? new Date(a.detectedAt).toLocaleString() : "-"}
                  </td>
                  <td><code>{a.alarmType}</code></td>
                  <td>
                    <span className={a.severity === "CRIT" ? "badge bad" : "badge"}>{a.severity}</span>
                  </td>
                  <td>
                    <span className={a.status === "OPEN" ? "badge bad" : (a.status === "ACK" ? "badge" : "badge ok")}>
                      {a.status}
                    </span>
                  </td>
                  <td><code>{a.tagId}</code></td>
                  <td><code>{a.stationCode}</code></td>
                  <td style={{ maxWidth: 360 }}>
                    <div style={{ whiteSpace: "pre-wrap" }}>{a.message ?? ""}</div>
                  </td>
                  <td>
                    <div className="row">
                      <input
                        className="input"
                        style={{ width: 160 }}
                        value={assignMap[a.id] ?? ""}
                        placeholder="assignee"
                        onChange={(e) => setAssignMap((p) => ({ ...p, [a.id]: e.target.value }))}
                      />
                      <button className="btn secondary" disabled={busyId === a.id} onClick={() => assign(a.id)}>저장</button>
                    </div>
                  </td>
                  <td>
                    <div className="row">
                      {a.status === "OPEN" && (
                        <button className="btn" disabled={busyId === a.id} onClick={() => ack(a.id)}>ACK</button>
                      )}
                      {a.status !== "CLOSED" && (
                        <button className="btn secondary" disabled={busyId === a.id} onClick={() => close(a.id)}>CLOSE</button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
              {!alarms.length && (
                <tr>
                  <td colSpan={9} className="muted">알람이 없습니다.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      <div className="card">
        <div className="muted">
          * 알람은 <code>OUT</code> 스캔 시 CycleTime SLA 초과, 또는 스케줄러가 정체(STUCK)를 감지하면 자동 생성됩니다.
          SSE eventName=<code>alarm</code>으로 갱신됩니다.
        </div>
      </div>
    </div>
  );
}
