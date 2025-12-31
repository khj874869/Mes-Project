import { useEffect, useState } from "react";
import { getJson } from "../lib/http";
import { CORE, HUB } from "../lib/api";

type Health = { status: string };

type Summary = {
  total: number;
  byStation: { stationCode: string; qty: number }[];
};

type Station = { stationCode: string; name: string; lineCode: string; seq: number };

export default function Dashboard() {
  const [core, setCore] = useState<Health | null>(null);
  const [hub, setHub] = useState<Health | null>(null);
  const [summary, setSummary] = useState<Summary | null>(null);
  const [stations, setStations] = useState<Station[]>([]);
  const [err, setErr] = useState<string | null>(null);

  const refresh = async () => {
    try {
      const [c, h, s, st] = await Promise.all([
        getJson<Health>(`${CORE}/actuator/health`),
        getJson<Health>(`${HUB}/actuator/health`),
        getJson<Summary>(`${CORE}/wip/summary`),
        getJson<Station[]>(`${CORE}/system/stations?active=true`)
      ]);
      setCore(c);
      setHub(h);
      setSummary(s);
      setStations(st);
      setErr(null);
    } catch (e: any) {
      setErr(e?.message ?? String(e));
    }
  };

  useEffect(() => {
    refresh();
  }, []);

  return (
    <div className="grid">
      <div className="card">
        <div className="row" style={{ justifyContent: "space-between" }}>
          <div>
            <h1 className="h1">MES 운영 대시보드</h1>
            <div className="muted">설비/공정 현황 · WIP 요약 · 연동 상태</div>
          </div>
          <button className="btn secondary" onClick={refresh}>새로고침</button>
        </div>
        {err && <div className="bad" style={{ marginTop: 10 }}>ERROR: {err}</div>}
      </div>

      <div className="grid grid-3">
        <div className="card">
          <div className="h2">mes-core</div>
          {!core && <div className="muted">loading…</div>}
          {core && <div className={core.status === "UP" ? "ok" : "bad"}>STATUS: {core.status}</div>}
          <div className="muted" style={{ marginTop: 8 }}><code>/actuator/health</code></div>
        </div>

        <div className="card">
          <div className="h2">integration-hub</div>
          {!hub && <div className="muted">loading…</div>}
          {hub && <div className={hub.status === "UP" ? "ok" : "bad"}>STATUS: {hub.status}</div>}
          <div className="muted" style={{ marginTop: 8 }}><code>/actuator/health</code></div>
        </div>

        <div className="card">
          <div className="h2">총 WIP</div>
          <div style={{ fontSize: 34, fontWeight: 800, marginTop: 6 }}>
            {summary ? summary.total.toLocaleString() : "-"}
          </div>
          <div className="muted">재공(WIP) · <code>wip_unit</code></div>
        </div>
      </div>

      <div className="grid grid-2">
        <div className="card">
          <div className="h2">스테이션</div>
          <div className="muted">DB(System Master) 기반</div>
          <div className="grid grid-2" style={{ marginTop: 10 }}>
            {stations.slice(0, 6).map((s) => (
              <div key={s.stationCode} className="kpi">
                <div className="kpi-title">{s.name}</div>
                <div className="kpi-sub muted">{s.lineCode} · {s.stationCode}</div>
              </div>
            ))}
            {!stations.length && <div className="muted">스테이션이 없습니다. (Admin → System에서 등록)</div>}
          </div>
        </div>

        <div className="card">
          <div className="h2">현장/연동 도구</div>
          <div className="grid" style={{ marginTop: 10 }}>
            <a className="btn secondary" href="http://localhost:8088" target="_blank" rel="noreferrer">Kafka UI</a>
            <div className="muted">토픽/메시지 확인용</div>
          </div>
          <div className="muted" style={{ marginTop: 12 }}>
            권장 플로우: <b>Admin → System</b>에서 라인/스테이션 등록 → <b>키오스크</b>에서 스캔 → <b>WIP Monitor</b>에서 실시간 확인
          </div>
        </div>
      </div>
    </div>
  );
}
