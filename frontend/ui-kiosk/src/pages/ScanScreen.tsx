import { useEffect, useRef, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { CORE } from "../lib/api";
import { postJson } from "../lib/http";

type Result = { ok: boolean; message: string; raw?: any };

export default function ScanScreen() {
  const { stationCode } = useParams();
  const inputRef = useRef<HTMLInputElement | null>(null);

  const [mode, setMode] = useState<"IN" | "OUT">("IN");
  const [tagId, setTagId] = useState("");
  const [last, setLast] = useState<Result | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    inputRef.current?.focus();
  }, [mode, stationCode]);

  const submit = async () => {
    if (!tagId.trim()) return;
    if (!stationCode) return;
    setBusy(true);
    setLast(null);

    const path = mode === "IN" ? `${CORE}/kiosk/scan/in` : `${CORE}/kiosk/scan/out`;
    const payload = {
      stationCode,
      tagId: tagId.trim(),
      direction: mode,
      occurredAt: new Date().toISOString()
    };

    const requestId = (globalThis.crypto && "randomUUID" in globalThis.crypto)
      ? (globalThis.crypto as any).randomUUID()
      : `req_${Date.now()}_${Math.random().toString(16).slice(2)}`;
    try {
      const res = await postJson<any>(path, payload, { "X-Request-Id": requestId });
      setLast({ ok: true, message: "처리 완료", raw: res });
    } catch (e: any) {
      setLast({ ok: false, message: e?.message ?? String(e) });
    } finally {
      setBusy(false);
      setTagId("");
      inputRef.current?.focus();
    }
  };

  return (
    <div className="grid">
      <div className="card">
        <div className="row" style={{ justifyContent: "space-between" }}>
          <div>
            <h1 className="h1">스캔</h1>
            <div className="muted">Station: <b>{stationCode}</b></div>
          </div>
          <Link className="btn secondary" to="/">스테이션 변경</Link>
        </div>
      </div>

      <div className="card">
        <div className="row">
          <button className={"btn " + (mode === "IN" ? "" : "secondary")} onClick={() => setMode("IN")}>IN</button>
          <button className={"btn " + (mode === "OUT" ? "" : "secondary")} onClick={() => setMode("OUT")}>OUT</button>
          <span className="pill muted">스캐너 입력 후 Enter</span>
        </div>

        <div style={{ marginTop: 12 }}>
          <input
            ref={inputRef}
            className="input"
            placeholder="TAG/Serial 입력 (스캐너)"
            value={tagId}
            onChange={(e) => setTagId(e.target.value)}
            onKeyDown={(e) => { if (e.key === "Enter") submit(); }}
          />
        </div>

        <div style={{ marginTop: 12 }}>
          <button className="btn" disabled={busy} onClick={submit}>
            {busy ? "처리중…" : "처리"}
          </button>
        </div>

        <div className="muted" style={{ marginTop: 12 }}>
          * 중복 스캔 방지를 위해 요청마다 <code>X-Request-Id</code> 멱등키를 전송합니다.
        </div>
      </div>

      <div className="card">
        <div className="h2">최근 결과</div>
        {!last && <div className="muted">없음</div>}
        {last && (
          <>
            <div className={last.ok ? "ok" : "bad"}>{last.ok ? "OK" : "ERROR"} - {last.message}</div>
            {last.raw && <pre className="muted" style={{ marginTop: 8 }}>{JSON.stringify(last.raw, null, 2)}</pre>}
          </>
        )}
      </div>
    </div>
  );
}
