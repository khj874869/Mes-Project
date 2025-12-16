import { useNavigate } from "react-router-dom";

const stations = [
  { code: "S01", name: "S01 투입" },
  { code: "S02", name: "S02 조립" },
  { code: "S03", name: "S03 검사" },
  { code: "S04", name: "S04 포장" }
];

export default function StationSelect() {
  const nav = useNavigate();

  return (
    <div className="grid">
      <div className="card">
        <h1 className="h1">스테이션 선택</h1>
        <div className="muted" style={{ marginTop: 6 }}>
          현장 키오스크는 “선택 → 반복 스캔” UX가 핵심이라, 화면을 최대한 단순하게 구성합니다.
        </div>
      </div>

      <div className="grid grid-2">
        {stations.map((s) => (
          <button
            key={s.code}
            className="card"
            style={{ cursor: "pointer", textAlign: "left" }}
            onClick={() => nav(`/station/${s.code}`)}
          >
            <div className="h2">{s.name}</div>
            <div className="muted">Code: {s.code}</div>
          </button>
        ))}
      </div>
    </div>
  );
}
