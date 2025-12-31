import { useEffect, useMemo, useState } from "react";
import { CORE } from "../lib/api";
import { getJson } from "../lib/http";
import { withAccessToken } from "../lib/sse";

type TelemetryEvent = {
  siteId: string;
  sensorId: string;
  metric: string;
  value: number;
  ts: string;
};

export default function AdminTelemetry() {
  const [siteId, setSiteId] = useState("SITE-01");
  const [stream, setStream] = useState<TelemetryEvent[]>([]);
  const [recent, setRecent] = useState<any[]>([]);
  const [status, setStatus] = useState("connecting...");

  const sseUrl = useMemo(() => withAccessToken(`${CORE}/admin/logs/stream`), []); // telemetry는 같은 SSE 허브에 eventName=telemetry
  useEffect(() => {
    const es = new EventSource(sseUrl);
    setStatus("connected");
    es.addEventListener("telemetry", (e: MessageEvent) => {
      try {
        const t = JSON.parse(e.data) as TelemetryEvent;
        setStream((prev) => {
          const next = [...prev, t];
          return next.length > 200 ? next.slice(next.length - 200) : next;
        });
      } catch {/* ignore */}
    });
    es.onerror = () => setStatus("disconnected (retrying...)");
    return () => es.close();
  }, [sseUrl]);

  const loadRecent = async () => {
    const data = await getJson<any[]>(`${CORE}/admin/telemetry/recent?siteId=${encodeURIComponent(siteId)}&limit=200`);
    setRecent(data);
  };

  useEffect(() => { loadRecent().catch(() => {}); }, [siteId]);

  return (
    <div className="grid">
      <div className="card">
        <h1 className="h1">현장 텔레메트리(온도/습도/전력)</h1>
        <div className="row">
          <label className="row">
            <span className="muted">Site</span>
            <input value={siteId} onChange={(e) => setSiteId(e.target.value)} style={{ width: 180 }} />
          </label>
          <button className="btn" onClick={loadRecent}>최근 데이터 불러오기</button>
          <span className="pill muted">SSE: {status}</span>
        </div>
        <div className="muted" style={{ marginTop: 6 }}>
          SSE 실시간 이벤트 + DB 저장(최근 조회)까지 포함합니다.
        </div>
      </div>

      <div className="grid grid-2">
        <div className="card" style={{ maxHeight: 420, overflow: "auto" }}>
          <div className="h2">실시간(최근 200)</div>
          <div className="muted" style={{ marginTop: 6 }}>eventName=telemetry</div>
          <table className="table" style={{ marginTop: 10 }}>
            <thead>
              <tr><th>ts</th><th>metric</th><th>value</th><th>sensor</th></tr>
            </thead>
            <tbody>
              {stream.slice().reverse().map((t, i) => (
                <tr key={i}>
                  <td>{new Date(t.ts).toLocaleTimeString()}</td>
                  <td>{t.metric}</td>
                  <td>{t.value}</td>
                  <td className="muted">{t.sensorId}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="card" style={{ maxHeight: 420, overflow: "auto" }}>
          <div className="h2">DB Recent (limit 200)</div>
          <table className="table" style={{ marginTop: 10 }}>
            <thead>
              <tr><th>ts</th><th>metric</th><th>value</th><th>sensor</th></tr>
            </thead>
            <tbody>
              {recent.map((t: any) => (
                <tr key={t.id}>
                  <td>{new Date(t.ts).toLocaleString()}</td>
                  <td>{t.metric}</td>
                  <td>{t.value}</td>
                  <td className="muted">{t.sensorId}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
