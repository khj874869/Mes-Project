import { useEffect, useState } from "react";
import { CORE } from "../lib/api";
import { getJson, postJson } from "../lib/http";

type UserView = {
  id: number;
  username: string;
  displayName: string;
  enabled: boolean;
  role: "ADMIN" | "USER";
  createdAt: string;
};

export default function AdminUsers() {
  const [users, setUsers] = useState<UserView[]>([]);
  const [err, setErr] = useState<string | null>(null);

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("pass1234");
  const [displayName, setDisplayName] = useState("");
  const [role, setRole] = useState<"ADMIN" | "USER">("USER");

  const load = async () => {
    setErr(null);
    try {
      const data = await getJson<UserView[]>(`${CORE}/admin/users`);
      setUsers(data);
    } catch (e: any) {
      setErr(String(e?.message ?? e));
    }
  };

  useEffect(() => { load(); }, []);

  const create = async () => {
    try {
      await postJson(`${CORE}/admin/users`, { username, password, displayName, role });
      setUsername(""); setDisplayName("");
      await load();
    } catch (e: any) {
      setErr(String(e?.message ?? e));
    }
  };

  const setEnabled = async (id: number, enabled: boolean) => {
    await postJson(`${CORE}/admin/users/${id}/enable`, { enabled });
    await load();
  };

  const setRoleApi = async (id: number, role: "ADMIN" | "USER") => {
    await postJson(`${CORE}/admin/users/${id}/role`, { role });
    await load();
  };

  return (
    <div className="grid">
      <div className="card">
        <h1 className="h1">사용자 관리(RBAC)</h1>
        <div className="muted" style={{ marginTop: 6 }}>
          관리자/일반 사용자 권한 분리 + 계정 활성/비활성화.
        </div>
        {err && <div className="pill" style={{ marginTop: 10 }}>{err}</div>}
      </div>

      <div className="card">
        <div className="h2">새 사용자 생성</div>
        <div className="grid grid-2" style={{ marginTop: 10 }}>
          <label>
            <div className="muted">Username</div>
            <input value={username} onChange={(e) => setUsername(e.target.value)} />
          </label>
          <label>
            <div className="muted">Display name</div>
            <input value={displayName} onChange={(e) => setDisplayName(e.target.value)} />
          </label>
          <label>
            <div className="muted">Password</div>
            <input value={password} onChange={(e) => setPassword(e.target.value)} />
          </label>
          <label>
            <div className="muted">Role</div>
            <select value={role} onChange={(e) => setRole(e.target.value as any)}>
              <option value="USER">USER</option>
              <option value="ADMIN">ADMIN</option>
            </select>
          </label>
        </div>

        <div className="row" style={{ marginTop: 10 }}>
          <button className="btn" onClick={create} disabled={!username || !displayName}>생성</button>
          <button className="btn" onClick={load}>새로고침</button>
        </div>
      </div>

      <div className="card" style={{ overflow: "auto" }}>
        <div className="h2">사용자 목록</div>
        <table className="table" style={{ marginTop: 10 }}>
          <thead>
            <tr>
              <th>ID</th><th>Username</th><th>Display</th><th>Role</th><th>Enabled</th><th>Created</th><th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.map(u => (
              <tr key={u.id}>
                <td>{u.id}</td>
                <td><strong>{u.username}</strong></td>
                <td>{u.displayName}</td>
                <td>{u.role}</td>
                <td>{u.enabled ? "Y" : "N"}</td>
                <td className="muted">{new Date(u.createdAt).toLocaleString()}</td>
                <td>
                  <div className="row" style={{ gap: 6, flexWrap: "wrap" }}>
                    <button className="btn" onClick={() => setEnabled(u.id, !u.enabled)}>
                      {u.enabled ? "Disable" : "Enable"}
                    </button>
                    <button className="btn" onClick={() => setRoleApi(u.id, u.role === "ADMIN" ? "USER" : "ADMIN")}>
                      Make {u.role === "ADMIN" ? "USER" : "ADMIN"}
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
