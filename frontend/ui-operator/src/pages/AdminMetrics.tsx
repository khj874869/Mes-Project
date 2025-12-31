import { useEffect, useState } from "react";
import { getJson } from "../lib/http";
import { CORE } from "../lib/api";

type Summary = {
  avgRpsSinceStart: number;
  totalRequests: number;
  total5xx: number;
  errorRate: number;
  p95Ms: number;
};

export default function AdminMetrics() {
  const [s, setS] = useState<Summary | null>(null);
  const [err, setErr] = useState<string | null>(null);

  useEffect(() => {
    getJson<Summary>(`${CORE}/admin/metrics/summary`).then(setS).catch((e) => setErr(String(e)));
  }, []);

  return (
    <div className="grid">
      <div className="card">
        <h1 className="h1">TPS / APM 요약</h1>
        <div className="muted" style={{ marginTop: 6 }}>
          Micrometer 기반의 단순 요약(서버 기동 이후 누적/평균)입니다.
        </div>
      </div>

      {err && <div className="card"><div className="pill">{err}</div></div>}

      {s && (
        <div className="card">
          <div className="grid grid-2">
            <div><div className="muted">Avg RPS (since start)</div><div className="h2">{s.avgRpsSinceStart.toFixed(2)}</div></div>
            <div><div className="muted">Total Requests</div><div className="h2">{s.totalRequests}</div></div>
            <div><div className="muted">Total 5xx</div><div className="h2">{s.total5xx}</div></div>
            <div><div className="muted">Error Rate</div><div className="h2">{(s.errorRate * 100).toFixed(2)}%</div></div>
            <div><div className="muted">p95 (ms)</div><div className="h2">{s.p95Ms.toFixed(1)}</div></div>
          </div>
        </div>
      )}
    </div>
  );
}
