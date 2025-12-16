import { useMemo, useState } from "react";
import { postJson } from "../lib/http";
import { HUB } from "../lib/api";


type Ack = { status: string; idempotencyKey: string } | any;

export default function ErpInboundTest() {
  const [requestId, setRequestId] = useState("erp-req-001");
  const [woNo, setWoNo] = useState("WO-10001");
  const [itemCode, setItemCode] = useState("ITEM-ABC");
  const [quantity, setQuantity] = useState(100);
  const [result, setResult] = useState<string>("");

  const body = useMemo(() => ({ woNo, itemCode, quantity }), [woNo, itemCode, quantity]);

  const send = async () => {
    setResult("sending…");
    try {
    const res = await postJson<Ack>(`${HUB}/erp/work-orders`, body, { "X-Request-Id": requestId });
      setResult(JSON.stringify(res, null, 2));
    } catch (e: any) {
      setResult(String(e?.message ?? e));
    }
  };

  return (
    <div className="grid">
      <div className="card">
        <h1 className="h1">ERP Inbound 테스트</h1>
        <div className="muted" style={{ marginTop: 6 }}>
        Gateway를 통해 <code>{HUB}/erp/work-orders</code>를 호출합니다.
        </div>
      </div>

      <div className="card">
        <div className="grid grid-2">
          <div>
            <div className="muted">X-Request-Id</div>
            <input className="input" value={requestId} onChange={(e) => setRequestId(e.target.value)} />
            <div className="muted" style={{ marginTop: 8 }}>
              같은 Request-Id로 2번 보내면 <b>DUPLICATE</b>가 나와야 정상
            </div>
          </div>

          <div>
            <div className="muted">Work Order</div>
            <div className="row">
              <input className="input" value={woNo} onChange={(e) => setWoNo(e.target.value)} />
              <input className="input" value={itemCode} onChange={(e) => setItemCode(e.target.value)} />
              <input className="input" type="number" value={quantity} onChange={(e) => setQuantity(Number(e.target.value))} />
            </div>
            <div className="muted" style={{ marginTop: 8 }}>
              현재 백엔드 DTO 기준 최소 필드: <code>woNo</code>, <code>itemCode</code>, <code>quantity</code>
            </div>
          </div>
        </div>

        <div style={{ marginTop: 12 }}>
          <button className="btn" onClick={send}>전송</button>
        </div>
      </div>

      <div className="card">
        <div className="h2">응답</div>
        <pre className="muted">{result}</pre>
      </div>
    </div>
  );
}
