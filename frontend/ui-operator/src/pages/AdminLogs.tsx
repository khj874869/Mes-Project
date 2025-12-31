import { useEffect, useMemo, useRef, useState } from "react";
import { CORE } from "../lib/api";
import { withAccessToken } from "../lib/sse";

type LogEvent = {
  ts: string;
  service: string;
  level: string;
  logger: string;
  thread: string;
  traceId?: string | null;
  msg: string;
  exception?: string | null;
};

export default function AdminLogs() {
  const [logs, setLogs] = useState<LogEvent[]>([]);
  const [status, setStatus] = useState<string>("connecting...");
  const bottomRef = useRef<HTMLDivElement | null>(null);

  const url = useMemo(() => withAccessToken(`${CORE}/admin/logs/stream`), []);

  useEffect(() => {
    const es = new EventSource(url, { withCredentials: false });
    setStatus("connected");

    es.addEventListener("log", (e: MessageEvent) => {
      try {
        const data = JSON.parse(e.data) as LogEvent;
        setLogs((prev) => {
          const next = [...prev, data];
          return next.length > 300 ? next.slice(next.length - 300) : next;
        });
      } catch { /* ignore */ }
    });

    es.onerror = () => setStatus("disconnected (retrying...)");

    return () => es.close();
  }, [url]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [logs.length]);

  return (
    <div className="grid">
      <div className="card">
        <h1 className="h1">실시간 로그 스트림</h1>
        <div className="row">
          <span className="pill muted">SSE</span>
          <span className="pill">{status}</span>
          <span className="pill muted">keep last 300</span>
        </div>
        <div className="muted" style={{ marginTop: 6 }}>
          백엔드(Logback → Kafka → mes-core consumer → SSE) 파이프라인으로 들어오는 로그를 보여줍니다.
        </div>
      </div>

      <div className="card" style={{ maxHeight: 520, overflow: "auto" }}>
        <div className="grid" style={{ gap: 10 }}>
          {logs.map((l, idx) => (
            <div key={idx} className="card" style={{ padding: 12 }}>
              <div className="row" style={{ justifyContent: "space-between" }}>
                <div>
                  <strong>{l.level}</strong>{" "}
                  <span className="muted">{new Date(l.ts).toLocaleString()}</span>
                </div>
                <div className="muted">{l.service}</div>
              </div>
              <div style={{ marginTop: 6, whiteSpace: "pre-wrap" }}>{l.msg}</div>
              {(l.exception) && (
                <div className="pill" style={{ marginTop: 8, whiteSpace: "pre-wrap" }}>
                  {l.exception}
                </div>
              )}
              <div className="muted" style={{ marginTop: 8, fontSize: 12 }}>
                {l.logger} · {l.thread} {l.traceId ? `· traceId=${l.traceId}` : ""}
              </div>
            </div>
          ))}
          <div ref={bottomRef} />
        </div>
      </div>
    </div>
  );
}
