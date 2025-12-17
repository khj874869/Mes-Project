import { Link, NavLink, Route, Routes, useLocation, Navigate } from "react-router-dom";
import Dashboard from "./pages/Dashboard";
import ErpInboundTest from "./pages/ErpInboundTest";
import Login from "./pages/Login";
import AdminUsers from "./pages/AdminUsers";
import AdminLogs from "./pages/AdminLogs";
import AdminTelemetry from "./pages/AdminTelemetry";
import AdminMetrics from "./pages/AdminMetrics";
import AdminResults from "./pages/AdminResults";
import { AuthProvider, useAuth } from "./lib/auth";

function Guard({ children, role }: { children: JSX.Element; role?: "ADMIN" | "USER" }) {
  const { me, loading } = useAuth();
  const loc = useLocation();

  if (loading) return <div className="card">Loading...</div>;
  if (!me) return <Navigate to="/login" replace state={{ from: loc.pathname }} />;
  if (role && me.role !== role) return <div className="card">Forbidden</div>;
  return children;
}

function Nav() {
  const { me, logout } = useAuth();

  return (
    <div className="nav">
      <div className="nav-inner">
        <div className="row">
          <strong>MES Operator</strong>
          <span className="pill muted">core :8080 · hub :8083</span>
          {me && <span className="pill">{me.displayName} ({me.role})</span>}
          {me && <button className="btn" onClick={logout}>Logout</button>}
        </div>

        <div className="nav-links">
          {me && (
            <>
              <NavLink className="pill" to="/">Dashboard</NavLink>
              <NavLink className="pill" to="/erp-inbound">ERP Inbound</NavLink>
            </>
          )}

          {me?.role === "ADMIN" && (
            <>
              <span className="pill muted">Admin</span>
              <NavLink className="pill" to="/admin/users">Users</NavLink>
              <NavLink className="pill" to="/admin/logs">Logs</NavLink>
              <NavLink className="pill" to="/admin/telemetry">Telemetry</NavLink>
              <NavLink className="pill" to="/admin/metrics">TPS/APM</NavLink>
              <NavLink className="pill" to="/admin/results">Results</NavLink>
            </>
          )}

          {!me && <NavLink className="pill" to="/login">Login</NavLink>}
        </div>
      </div>
    </div>
  );
}

function AppRoutes() {
  return (
    <div className="container">
      <Routes>
        <Route path="/login" element={<Login />} />

        <Route path="/" element={<Guard><Dashboard /></Guard>} />
        <Route path="/erp-inbound" element={<Guard><ErpInboundTest /></Guard>} />

        <Route path="/admin/users" element={<Guard role="ADMIN"><AdminUsers /></Guard>} />
        <Route path="/admin/logs" element={<Guard role="ADMIN"><AdminLogs /></Guard>} />
        <Route path="/admin/telemetry" element={<Guard role="ADMIN"><AdminTelemetry /></Guard>} />
        <Route path="/admin/metrics" element={<Guard role="ADMIN"><AdminMetrics /></Guard>} />
        <Route path="/admin/results" element={<Guard role="ADMIN"><AdminResults /></Guard>} />

        <Route path="*" element={<div className="card">Not found. <Link to="/">Go home</Link></div>} />
      </Routes>
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <Nav />
      <AppRoutes />
    </AuthProvider>
  );
}
