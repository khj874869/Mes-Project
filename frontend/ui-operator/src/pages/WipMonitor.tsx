import { useEffect, useMemo, useRef, useState } from "react";
import { CORE } from "../lib/api";
import { getJson } from "../lib/http";
import { withAccessToken } from "../lib/sse";

type Station = {
  stationCode: string;
  name: string;
  lineCode: string;
  seq: number;
  kioskEnabled: boolean;
  active: boolean;
};

type Summary = {
  total: number;
  byStation: { stationCode: string; qty: number }[];
};

type Event = {
  eventId: string;
  tagId: string;
  stationCode: string;
  direction: "IN" | "OUT" | string;
  occurredAt: string;
};

export default function WipMonitor() {
  const [stations, setStations] = useState<Station[]>([]);
  const [summary, setSummary] = useState<Summary | null>(null);
  const [events, setEvents] = useState<Event[]>([]);
  const [err, setErr] = useState<string | null>(null);

  const [q, setQ] = useState("");
  const [stationFilter, setStationFilter] = useState("ALL");

  const esRef = useRef<EventSource | null>(null);

  const byStationMap = useMemo(() => {
    const m = new Map<string, number>();
    summary?.byStation?.forEach((x) => m.set(x.stationCode ?? "-", x.qty));
    return m;
  }, [summary]);

  const refresh = async () => {
    try {
      const [st, sm, ev] = await Promise.all([
        getJson<Station[]>(`${CORE}/system/stations?active=true`),
        getJson<Summary>(`${CORE}/wip/summary`),
        getJson<Event[]>(`${CORE}/wip/events?limit=60`)
      ]);
      setStations(st);
      setSummary(sm);
      setEvents(ev);
      setErr(null);
    } catch (e: any) {
      setErr(e?.message ?? String(e));
    }
  };

  useEffect(() => {
    refresh();
  }, []);

  const filteredEvents = useMemo(() => {
    const query = q.trim().toLowerCase();
    return events.filter((e) => {
      if (stationFilter !== "ALL" && e.stationCode !== stationFilter) return false;
      if (!query) return true;
      return (e.tagId ?? "").toLowerCase().includes(query) || (e.eventId ?? "").toLowerCase().includes(query);
    });
  }, [events, q, stationFilter]);

  useEffect(() => {
    // 실시간: SSE (kiosk scan 시 wip 이벤트 broadcast)
    const es = new EventSource(withAccessToken(`${CORE}/wip/stream`));
    esRef.current = es;

    const onWip = (ev: MessageEvent) => {
      try {
        const data = JSON.parse(ev.data);
        const e: Event = {
          eventId: data.eventId ?? "",
          tagId: data.tagId ?? "",
          stationCode: data.stationCode ?? "",
          direction: data.direction ?? "",
          occurredAt: data.occurredAt ?? new Date().toISOString()
        };
        setEvents((prev) => [e, ...prev].slice(0, 80));
        // summary는 간단히 다시 읽어오는 방식(데모)
        getJson<Summary>(`${CORE}/wip/summary`).then(setSummary).catch(() => {});
      } catch {
        // ignore
      }
    };

    es.addEventListener("wip", onWip as any);
    es.onerror = () => {
      // 네트워크/권한 문제 등
    };

    return () => {
      es.removeEventListener("wip", onWip as any);
      es.close();
      esRef.current = null;
    };
  }, []);

  return (
    <div className="grid">
      <div className="card">
        <div className="row" style={{ justifyContent: "space-between" }}>
          <div>
            <h1 className="h1">현장 모니터링</h1>
            <div className="muted">WIP 요약 · 스테이션별 재공 수량 · 실시간 스캔 타임라인</div>
          </div>
          <button className="btn secondary" onClick={refresh}>새로고침</button>
        </div>
        {err && <div className="bad" style={{ marginTop: 10 }}>ERROR: {err}</div>}
      </div>

      <div className="grid grid-3">
        <div className="card">
          <div className="h2">총 WIP</div>
          <div style={{ fontSize: 34, fontWeight: 800, marginTop: 6 }}>
            {summary ? summary.total.toLocaleString() : "-"}
          </div>
          <div className="muted">wip_unit 기준</div>
        </div>

        <div className="card">
          <div className="h2">실시간 연결</div>
          <div className={esRef.current ? "ok" : "muted"}>
            {esRef.current ? "SSE CONNECTED" : "SSE"}
          </div>
          <div className="muted" style={{ marginTop: 6 }}>
            Kiosk 스캔 시 <code>wip</code> 이벤트로 푸시됩니다.
          </div>
        </div>

        <div className="card">
          <div className="h2">스테이션 수</div>
          <div style={{ fontSize: 34, fontWeight: 800, marginTop: 6 }}>
            {stations.length}
          </div>
          <div className="muted">system master (DB)</div>
        </div>
      </div>

      <div className="card">
        <div className="h2">스테이션별 재공</div>
        <div className="grid grid-3" style={{ marginTop: 10 }}>
          {stations.map((s) => (
            <div key={s.stationCode} className="kpi">
              <div className="kpi-title">{s.name}</div>
              <div className="kpi-sub muted">{s.stationCode} · {s.lineCode}</div>
              <div className="kpi-value">{(byStationMap.get(s.stationCode) ?? 0).toLocaleString()}</div>
            </div>
          ))}
          {!stations.length && <div className="muted">스테이션이 없습니다. (Admin → System에서 등록)</div>}
        </div>
      </div>

      <div className="card">
        <div className="h2">최근 스캔 타임라인</div>

        <div className="row" style={{ justifyContent: "space-between", marginBottom: 10 }}>
          <div className="muted">최신 {events.length}건</div>
          <div className="row">
            <select className="input" style={{ width: 180 }} value={stationFilter} onChange={(e) => setStationFilter(e.target.value)}>
              <option value="ALL">전체 스테이션</option>
              {stations.map((s) => (
                <option key={s.stationCode} value={s.stationCode}>{s.stationCode} - {s.name}</option>
              ))}
            </select>
            <input
              className="input"
              style={{ width: 240 }}
              placeholder="Tag / EventId 검색"
              value={q}
              onChange={(e) => setQ(e.target.value)}
            />
          </div>
        </div>

        <div style={{ overflowX: "auto" }}>
          <table className="table">
            <thead>
              <tr>
                <th>시간</th>
                <th>Tag</th>
                <th>Station</th>
                <th>Dir</th>
                <th>EventId</th>
              </tr>
            </thead>
            <tbody>
              {filteredEvents.map((e, idx) => (
                <tr key={`${e.eventId}-${idx}`}>
                  <td className="muted">{new Date(e.occurredAt).toLocaleString()}</td>
                  <td><code>{e.tagId}</code></td>
                  <td><code>{e.stationCode}</code></td>
                  <td>
                    <span className={e.direction === "IN" ? "badge ok" : "badge bad"}>
                      {e.direction}
                    </span>
                  </td>
                  <td className="muted" style={{ maxWidth: 260, overflow: "hidden", textOverflow: "ellipsis" }}>
                    {e.eventId}
                  </td>
                </tr>
              ))}
              {!filteredEvents.length && (
                <tr>
                  <td colSpan={5} className="muted">최근 이벤트가 없습니다.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
