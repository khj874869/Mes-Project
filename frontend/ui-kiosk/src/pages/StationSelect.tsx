import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { CORE } from "../lib/api";
import { getJson } from "../lib/http";

type Station = {
  stationCode: string;
  name: string;
  lineCode: string;
  seq: number;
  kioskEnabled: boolean;
  active: boolean;
};

export default function StationSelect() {
  const nav = useNavigate();
  const [stations, setStations] = useState<Station[]>([]);
  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const ordered = useMemo(() => {
    return [...stations]
      .filter((s) => s.active)
      .sort((a, b) => a.lineCode.localeCompare(b.lineCode) || (a.seq - b.seq));
  }, [stations]);

  const refresh = async () => {
    setLoading(true);
    try {
      const st = await getJson<Station[]>(`${CORE}/system/stations?active=true&kiosk=true`);
      setStations(st);
      setErr(null);
    } catch (e: any) {
      setErr(e?.message ?? String(e));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refresh();
  }, []);

  return (
    <div className="grid">
      <div className="card">
        <h1 className="h1">스테이션 선택</h1>
        <div className="row" style={{ justifyContent: "space-between", marginTop: 8 }}>
          <div className="muted">
            DB(System Master)에 등록된 스테이션을 불러옵니다.
          </div>
          <button className="btn secondary" onClick={refresh} disabled={loading}>새로고침</button>
        </div>
        {err && <div className="bad" style={{ marginTop: 10 }}>ERROR: {err}</div>}
      </div>

      <div className="grid grid-2">
        {loading && (
          <div className="card">
            <div className="muted">불러오는 중…</div>
          </div>
        )}

        {!loading && !ordered.length && (
          <div className="card">
            <div className="h2">스테이션이 없습니다</div>
            <div className="muted" style={{ marginTop: 6 }}>
              Operator(Admin)에서 System Master에 스테이션을 등록하고 <b>kioskEnabled</b>를 ON으로 설정하세요.
            </div>
          </div>
        )}

        {ordered.map((s) => (
          <button
            key={s.stationCode}
            className="card"
            style={{ cursor: "pointer", textAlign: "left" }}
            onClick={() => nav(`/station/${s.stationCode}`)}
          >
            <div className="h2">{s.name}</div>
            <div className="muted">{s.stationCode} · {s.lineCode} · seq {s.seq}</div>
          </button>
        ))}
      </div>
    </div>
  );
}
