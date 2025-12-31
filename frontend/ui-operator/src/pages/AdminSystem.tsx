import { useEffect, useState } from "react";
import { CORE } from "../lib/api";
import { getJson, postJson } from "../lib/http";

type Line = { lineCode: string; name: string; active: boolean };

type Station = {
  stationCode: string;
  name: string;
  lineCode: string;
  seq: number;
  kioskEnabled: boolean;
  active: boolean;
};

type StationSla = {
  stationCode: string;
  maxCycleSec: number;
  maxStuckSec: number;
  manualCheckSec: number;
  autoCheckSec: number;
  idealCycleSec: number;
  manning: number;
};

export default function AdminSystem() {
  const [lines, setLines] = useState<Line[]>([]);
  const [stations, setStations] = useState<Station[]>([]);
  const [slaList, setSlaList] = useState<StationSla[]>([]);
  const [err, setErr] = useState<string | null>(null);
  const [msg, setMsg] = useState<string | null>(null);

  const [lineForm, setLineForm] = useState<Line>({ lineCode: "L01", name: "Main Line", active: true });
  const [stForm, setStForm] = useState<Station>({ stationCode: "S01", name: "투입", lineCode: "L01", seq: 10, kioskEnabled: true, active: true });
  const [slaForm, setSlaForm] = useState<StationSla>({ stationCode: "S01", maxCycleSec: 300, maxStuckSec: 600, manualCheckSec: 30, autoCheckSec: 5, idealCycleSec: 120, manning: 1 });

  const refresh = async () => {
    try {
      const [l, s, sla] = await Promise.all([
        getJson<Line[]>(`${CORE}/admin/system/lines`),
        getJson<Station[]>(`${CORE}/admin/system/stations`),
        getJson<any[]>(`${CORE}/admin/process/sla`)
      ]);
      setLines(l);
      setStations(s);
      setSlaList(sla as StationSla[]);
      setErr(null);
    } catch (e: any) {
      setErr(e?.message ?? String(e));
    }
  };

  useEffect(() => {
    refresh();
  }, []);

  const saveLine = async () => {
    setMsg(null);
    try {
      await postJson(`${CORE}/admin/system/lines`, lineForm);
      setMsg("라인 저장 완료");
      await refresh();
    } catch (e: any) {
      setErr(e?.message ?? String(e));
    }
  };

  const saveStation = async () => {
    setMsg(null);
    try {
      await postJson(`${CORE}/admin/system/stations`, stForm);
      setMsg("스테이션 저장 완료");
      await refresh();
    } catch (e: any) {
      setErr(e?.message ?? String(e));
    }
  };

  const saveSla = async () => {
    setMsg(null);
    try {
      await postJson(`${CORE}/admin/process/sla`, slaForm);
      setMsg("SLA 저장 완료");
      await refresh();
    } catch (e: any) {
      setErr(e?.message ?? String(e));
    }
  };

  return (
    <div className="grid">
      <div className="card">
        <div className="row" style={{ justifyContent: "space-between" }}>
          <div>
            <h1 className="h1">System Master</h1>
            <div className="muted">라인/스테이션을 DB에 저장하고, Kiosk에서 실시간으로 불러옵니다.</div>
          </div>
          <button className="btn secondary" onClick={refresh}>새로고침</button>
        </div>
        {err && <div className="bad" style={{ marginTop: 10 }}>ERROR: {err}</div>}
        {msg && <div className="ok" style={{ marginTop: 10 }}>{msg}</div>}
      </div>

      <div className="grid grid-2">
        <div className="card">
          <div className="h2">라인 등록/수정</div>

          <div className="grid" style={{ marginTop: 10 }}>
            <label>
              <div className="muted">lineCode</div>
              <input className="input" value={lineForm.lineCode} onChange={(e) => setLineForm({ ...lineForm, lineCode: e.target.value })} />
            </label>
            <label>
              <div className="muted">name</div>
              <input className="input" value={lineForm.name} onChange={(e) => setLineForm({ ...lineForm, name: e.target.value })} />
            </label>
            <label className="row">
              <input type="checkbox" checked={lineForm.active} onChange={(e) => setLineForm({ ...lineForm, active: e.target.checked })} />
              <span>active</span>
            </label>
            <button className="btn" onClick={saveLine}>저장</button>
          </div>

          <div className="h2" style={{ marginTop: 18 }}>라인 목록</div>
          <table className="table" style={{ marginTop: 8 }}>
            <thead>
              <tr>
                <th>code</th>
                <th>name</th>
                <th>active</th>
              </tr>
            </thead>
            <tbody>
              {lines.map((l) => (
                <tr key={l.lineCode} onClick={() => setLineForm(l)} style={{ cursor: "pointer" }}>
                  <td><code>{l.lineCode}</code></td>
                  <td>{l.name}</td>
                  <td>{l.active ? <span className="badge ok">ON</span> : <span className="badge bad">OFF</span>}</td>
                </tr>
              ))}
              {!lines.length && (
                <tr><td colSpan={3} className="muted">없음</td></tr>
              )}
            </tbody>
          </table>
        </div>

        <div className="card">
          <div className="h2">스테이션 등록/수정</div>

          <div className="grid" style={{ marginTop: 10 }}>
            <label>
              <div className="muted">stationCode</div>
              <input className="input" value={stForm.stationCode} onChange={(e) => setStForm({ ...stForm, stationCode: e.target.value })} />
            </label>
            <label>
              <div className="muted">name</div>
              <input className="input" value={stForm.name} onChange={(e) => setStForm({ ...stForm, name: e.target.value })} />
            </label>
            <div className="grid grid-2" style={{ gap: 10 }}>
              <label>
                <div className="muted">lineCode</div>
                <input className="input" value={stForm.lineCode} onChange={(e) => setStForm({ ...stForm, lineCode: e.target.value })} />
              </label>
              <label>
                <div className="muted">seq</div>
                <input className="input" type="number" value={stForm.seq} onChange={(e) => setStForm({ ...stForm, seq: Number(e.target.value) })} />
              </label>
            </div>

            <div className="row">
              <label className="row">
                <input type="checkbox" checked={stForm.kioskEnabled} onChange={(e) => setStForm({ ...stForm, kioskEnabled: e.target.checked })} />
                <span>kioskEnabled</span>
              </label>
              <label className="row">
                <input type="checkbox" checked={stForm.active} onChange={(e) => setStForm({ ...stForm, active: e.target.checked })} />
                <span>active</span>
              </label>
            </div>

            <button className="btn" onClick={saveStation}>저장</button>
          </div>

          <div className="h2" style={{ marginTop: 18 }}>스테이션 목록</div>
          <div style={{ overflowX: "auto" }}>
            <table className="table" style={{ marginTop: 8 }}>
              <thead>
                <tr>
                  <th>code</th>
                  <th>name</th>
                  <th>line</th>
                  <th>seq</th>
                  <th>kiosk</th>
                  <th>active</th>
                </tr>
              </thead>
              <tbody>
                {stations.map((s) => (
                  <tr key={s.stationCode} onClick={() => setStForm(s)} style={{ cursor: "pointer" }}>
                    <td><code>{s.stationCode}</code></td>
                    <td>{s.name}</td>
                    <td><code>{s.lineCode}</code></td>
                    <td className="muted">{s.seq}</td>
                    <td>{s.kioskEnabled ? <span className="badge ok">ON</span> : <span className="badge bad">OFF</span>}</td>
                    <td>{s.active ? <span className="badge ok">ON</span> : <span className="badge bad">OFF</span>}</td>
                  </tr>
                ))}
                {!stations.length && (
                  <tr><td colSpan={6} className="muted">없음</td></tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="row" style={{ justifyContent: "space-between" }}>
          <div>
            <div className="h2">Process SLA (무인 공정 체크 기준)</div>
            <div className="muted">
              OUT CycleTime이 <code>maxCycleSec</code>을 초과하거나, IN 이후 <code>maxStuckSec</code>동안 OUT이 없으면 STUCK 알람이 자동 생성됩니다.
              시간 단축(KPI)은 <code>(manualCheckSec - autoCheckSec) × OUT</code>으로 계산됩니다.
            </div>
          </div>
          <button className="btn secondary" onClick={refresh}>새로고침</button>
        </div>

        <div className="grid" style={{ marginTop: 12 }}>
          <div className="grid grid-3" style={{ gap: 10 }}>
            <label>
              <div className="muted">stationCode</div>
              <input className="input" value={slaForm.stationCode} onChange={(e) => setSlaForm({ ...slaForm, stationCode: e.target.value })} />
            </label>
            <label>
              <div className="muted">maxCycleSec</div>
              <input className="input" type="number" value={slaForm.maxCycleSec} onChange={(e) => setSlaForm({ ...slaForm, maxCycleSec: Number(e.target.value) })} />
            </label>
            <label>
              <div className="muted">maxStuckSec</div>
              <input className="input" type="number" value={slaForm.maxStuckSec} onChange={(e) => setSlaForm({ ...slaForm, maxStuckSec: Number(e.target.value) })} />
            </label>
          </div>

          <div className="grid grid-3" style={{ gap: 10 }}>
            <label>
              <div className="muted">manualCheckSec</div>
              <input className="input" type="number" value={slaForm.manualCheckSec} onChange={(e) => setSlaForm({ ...slaForm, manualCheckSec: Number(e.target.value) })} />
            </label>
            <label>
              <div className="muted">autoCheckSec</div>
              <input className="input" type="number" value={slaForm.autoCheckSec} onChange={(e) => setSlaForm({ ...slaForm, autoCheckSec: Number(e.target.value) })} />
            </label>
            <label>
              <div className="muted">idealCycleSec</div>
              <input className="input" type="number" value={slaForm.idealCycleSec} onChange={(e) => setSlaForm({ ...slaForm, idealCycleSec: Number(e.target.value) })} />
            </label>
          </div>

          <div className="grid grid-3" style={{ gap: 10 }}>
            <label>
              <div className="muted">manning</div>
              <input className="input" type="number" value={slaForm.manning} onChange={(e) => setSlaForm({ ...slaForm, manning: Number(e.target.value) })} />
            </label>
            <div />
            <div className="row" style={{ alignItems: "end", justifyContent: "flex-end" }}>
              <button className="btn" onClick={saveSla}>SLA 저장</button>
            </div>
          </div>
        </div>

        <div className="h2" style={{ marginTop: 18 }}>SLA 목록</div>
        <div style={{ overflowX: "auto" }}>
          <table className="table" style={{ marginTop: 8 }}>
            <thead>
              <tr>
                <th>station</th>
                <th>maxCycleSec</th>
                <th>maxStuckSec</th>
                <th>manualCheckSec</th>
                <th>autoCheckSec</th>
                <th>idealCycleSec</th>
                <th>manning</th>
              </tr>
            </thead>
            <tbody>
              {slaList.map((s) => (
                <tr key={s.stationCode} onClick={() => setSlaForm(s)} style={{ cursor: "pointer" }}>
                  <td><code>{s.stationCode}</code></td>
                  <td>{s.maxCycleSec}</td>
                  <td>{s.maxStuckSec}</td>
                  <td>{s.manualCheckSec}</td>
                  <td>{s.autoCheckSec}</td>
                  <td>{s.idealCycleSec}</td>
                  <td>{s.manning}</td>
                </tr>
              ))}
              {!slaList.length && (
                <tr><td colSpan={7} className="muted">없음</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
