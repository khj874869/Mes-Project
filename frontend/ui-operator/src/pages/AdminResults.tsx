import { useEffect, useState } from "react";
import { CORE } from "../lib/api";
import { getJson } from "../lib/http";

type Row = {
  id: number;
  workOrderNo: string;
  lineCode: string;
  stationCode?: string | null;
  itemCode: string;
  qtyGood: number;
  qtyNg: number;
  createdAt: string;
};

export default function AdminResults() {
  const [rows, setRows] = useState<Row[]>([]);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  const loadMore = async () => {
    if (loading) return;
    setLoading(true);
    setErr(null);
    try {
      const last = rows[rows.length - 1];
      const qs = new URLSearchParams();
      qs.set("limit", "100");
      if (last) {
        qs.set("cursorCreatedAt", last.createdAt);
        qs.set("cursorId", String(last.id));
      }
      const data = await getJson<Row[]>(`${CORE}/admin/results?${qs.toString()}`);
      setRows((prev) => [...prev, ...data]);
    } catch (e: any) {
      setErr(String(e?.message ?? e));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadMore(); }, []);

  return (
    <div className="grid">
      <div className="card">
        <h1 className="h1">실적 데이터(50만+ 대응)</h1>
        <div className="muted" style={{ marginTop: 6 }}>
          DB에서는 (created_at,id) 키셋 페이징으로 조회합니다.
        </div>
        <div className="row" style={{ marginTop: 10 }}>
          <button className="btn" onClick={loadMore} disabled={loading}>
            {loading ? "로딩..." : "더 불러오기"}
          </button>
          {err && <span className="pill">{err}</span>}
        </div>
      </div>

      <div className="card" style={{ overflow: "auto" }}>
        <table className="table">
          <thead>
            <tr>
              <th>ID</th><th>Created</th><th>WO</th><th>Line</th><th>Station</th><th>Item</th><th>Good</th><th>NG</th>
            </tr>
          </thead>
          <tbody>
            {rows.map(r => (
              <tr key={r.id}>
                <td>{r.id}</td>
                <td className="muted">{new Date(r.createdAt).toLocaleString()}</td>
                <td><strong>{r.workOrderNo}</strong></td>
                <td>{r.lineCode}</td>
                <td className="muted">{r.stationCode ?? "-"}</td>
                <td>{r.itemCode}</td>
                <td>{r.qtyGood}</td>
                <td>{r.qtyNg}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
