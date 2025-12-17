import { useEffect, useState } from "react";
import { getJson } from "../lib/http";
import { HUB } from "../lib/api";

type Health = { status: string };

export default function Dashboard() {
  const [health, setHealth] = useState<Health | null>(null);
  const [err, setErr] = useState<string | null>(null);

  useEffect(() => {
    getJson<Health>(`${HUB}/actuator/health`)
      .then(setHealth)
      .catch((e) => setErr(String(e)));
  }, []);


 return (
    <div className="grid">
      <div className="card">
        <h1 className="h1">운영자 대시보드 (MVP)</h1>
        <div className="muted" style={{ marginTop: 6 }}>
          지금은 백엔드(read API)가 아직 최소 수준이라, <b>상태 확인 + ERP 인바운드 테스트</b>부터 제공합니다.
        </div>
      </div>

      <div className="grid grid-3">
        <div className="card">
          <div className="h2">Integration Hub 상태</div>
          {!health && !err && <div className="muted">loading…</div>}
          {health && <div className={health.status === "UP" ? "ok" : "bad"}>STATUS: {health.status}</div>}
          {err && <div className="bad">ERROR: {err}</div>}
          <div className="muted" style={{ marginTop: 8 }}>
            연결 확인용: <code>/actuator/health</code>
          </div>
        </div>

        <div className="card">
          <div className="h2">Kafka</div>
          <div className="muted">Kafka UI에서 토픽/메시지 확인</div>
          <div style={{ marginTop: 10 }}>
            <a className="btn secondary" href="http://localhost:8088" target="_blank" rel="noreferrer">Kafka UI 열기</a>
          </div>
        </div>

        <div className="card">
          <div className="h2">다음 단계</div>
          <div className="muted">
            1) mes-core에 kiosk API 추가<br/>
            2) read-model(라인/WIP) 테이블 구축<br/>
            3) 대시보드에 실시간 타임라인(SSE)
          </div>
        </div>
      </div>

      <div className="card">
        <div className="h2">메뉴 안내</div>
        <div className="muted">
          상단 메뉴 <b>ERP Inbound</b>에서 작업지시 upsert를 직접 쏴보고, 멱등(DUPLICATE)도 확인할 수 있어요.
        </div>
      </div>
    </div>
  );
}
