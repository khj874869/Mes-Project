import type { ReactNode } from "react";
import { Link, NavLink, Route, Routes, useLocation, Navigate } from "react-router-dom";
import Dashboard from "./pages/Dashboard";
import ErpInboundTest from "./pages/ErpInboundTest";
import Login from "./pages/Login";
import AdminUsers from "./pages/AdminUsers";
import AdminLogs from "./pages/AdminLogs";
import AdminTelemetry from "./pages/AdminTelemetry";
import AdminMetrics from "./pages/AdminMetrics";
import AdminResults from "./pages/AdminResults";
import WipMonitor from "./pages/WipMonitor";
import AdminSystem from "./pages/AdminSystem";
import Alarms from "./pages/Alarms";
import Performance from "./pages/Performance";
import { AuthProvider, useAuth } from "./lib/auth";

function Guard({ children, role }: { children: JSX.Element; role?: "ADMIN" | "USER" }) {
  const { me, loading } = useAuth();
  const loc = useLocation();

  if (loading) return <div className="card">Loading...</div>;
  if (!me) return <Navigate to="/login" replace state={{ from: loc.pathname }} />;
  if (role && me.role !== role) return <div className="card">Forbidden</div>;
  return children;
}

function AppShell({ children }: { children: ReactNode }) {
  const { me, logout } = useAuth();
  const loc = useLocation();
  const isAuthPage = loc.pathname.startsWith("/login");

  const navItem = ({ isActive }: { isActive: boolean }) =>
    "nav-item" + (isActive ? " active" : "");

  if (!me || isAuthPage) {
    return <div className="auth-wrap">{children}</div>;
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-title">MES Operator</div>
          <div className="brand-sub">core :8080 · hub :8083</div>
        </div>

        <div className="side-section">
          <div className="side-label">Operator</div>
          <NavLink className={navItem as any} to="/">Dashboard</NavLink>
          <NavLink className={navItem as any} to="/wip">WIP Monitor</NavLink>
          <NavLink className={navItem as any} to="/alarms">Alarm Center</NavLink>
          <NavLink className={navItem as any} to="/performance">Performance KPI</NavLink>
          <NavLink className={navItem as any} to="/erp-inbound">ERP Inbound</NavLink>
        </div>

        {me.role === "ADMIN" && (
          <div className="side-section">
            <div className="side-label">Admin</div>
            <NavLink className={navItem as any} to="/admin/users">Users</NavLink>
            <NavLink className={navItem as any} to="/admin/system">System</NavLink>
            <NavLink className={navItem as any} to="/admin/logs">Logs</NavLink>
            <NavLink className={navItem as any} to="/admin/telemetry">Telemetry</NavLink>
            <NavLink className={navItem as any} to="/admin/metrics">TPS / APM</NavLink>
            <NavLink className={navItem as any} to="/admin/results">Results</NavLink>
          </div>
        )}

        <div className="side-footer">
          <div className="muted" style={{ fontSize: 12 }}>
            Signed in as <b style={{ color: "#fff" }}>{me.displayName}</b> ({me.role})
          </div>
          <button className="btn secondary" style={{ width: "100%", marginTop: 10 }} onClick={logout}>
            Logout
          </button>
        </div>
      </aside>

      <div className="main">
        <header className="topbar">
          <div className="row">
            <span className="pill">운영 콘솔</span>
            <span className="pill muted">Real-time · WIP · Admin</span>
          </div>
          <div className="row">
            <span className="pill">{me.username}</span>
          </div>
        </header>

        <div className="content">
          <div className="container">{children}</div>
        </div>
      </div>
    </div>
  );
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />

      <Route path="/" element={<Guard><Dashboard /></Guard>} />
      <Route path="/wip" element={<Guard><WipMonitor /></Guard>} />
      <Route path="/alarms" element={<Guard><Alarms /></Guard>} />
      <Route path="/performance" element={<Guard><Performance /></Guard>} />
      <Route path="/erp-inbound" element={<Guard><ErpInboundTest /></Guard>} />

      <Route path="/admin/users" element={<Guard role="ADMIN"><AdminUsers /></Guard>} />
      <Route path="/admin/system" element={<Guard role="ADMIN"><AdminSystem /></Guard>} />
      <Route path="/admin/logs" element={<Guard role="ADMIN"><AdminLogs /></Guard>} />
      <Route path="/admin/telemetry" element={<Guard role="ADMIN"><AdminTelemetry /></Guard>} />
      <Route path="/admin/metrics" element={<Guard role="ADMIN"><AdminMetrics /></Guard>} />
      <Route path="/admin/results" element={<Guard role="ADMIN"><AdminResults /></Guard>} />

      <Route path="*" element={<div className="card">Not found. <Link to="/">Go home</Link></div>} />
    </Routes>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <AppShell>
        <AppRoutes />
      </AppShell>
    </AuthProvider>
  );
}
