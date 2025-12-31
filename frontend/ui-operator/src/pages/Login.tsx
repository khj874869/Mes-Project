import { useState } from "react";
import { useAuth } from "../lib/auth";

export default function Login() {
  const { login } = useAuth();
  const [username, setUsername] = useState("admin");
  const [password, setPassword] = useState("admin1234");
  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErr(null);
    setLoading(true);
    try {
      await login(username, password);
    } catch (e: any) {
      setErr(String(e?.message ?? e));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-card grid">
      <div className="card">
        <h1 className="h1">로그인</h1>
        <div className="muted" style={{ marginTop: 6 }}>
          기본 계정(초기 데이터): <code>admin / admin1234</code>
        </div>
      </div>

      <div className="card">
        <form className="grid" onSubmit={onSubmit}>
          <label>
            <div className="muted">Username</div>
            <input className="input" value={username} onChange={(e) => setUsername(e.target.value)} />
          </label>
          <label>
            <div className="muted">Password</div>
            <input className="input" type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
          </label>

          <button className="btn" disabled={loading}>
            {loading ? "로그인 중..." : "로그인"}
          </button>

          {err && <div className="pill" style={{ whiteSpace: "pre-wrap" }}>{err}</div>}
        </form>
      </div>
    </div>
  );
}
