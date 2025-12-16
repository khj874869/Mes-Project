import { Link, Route, Routes, NavLink } from "react-router-dom";
import Dashboard from "./pages/Dashboard";
import ErpInboundTest from "./pages/ErpInboundTest";

function Nav() {
  return (
    <div className="nav">
      <div className="nav-inner">
        <div className="row">
          <strong>MES Operator</strong>
          <span className="pill muted">dev proxy → :8083</span>
        </div>
        <div className="nav-links">
          <NavLink className="pill" to="/">Dashboard</NavLink>
          <NavLink className="pill" to="/erp-inbound">ERP Inbound</NavLink>
          <a className="pill" href="http://localhost:8088" target="_blank" rel="noreferrer">Kafka UI</a>
        </div>
      </div>
    </div>
  );
}

export default function App() {
  return (
    <>
      <Nav />
      <div className="container">
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/erp-inbound" element={<ErpInboundTest />} />
          <Route path="*" element={<div className="card">Not found. <Link to="/">Go home</Link></div>} />
        </Routes>
      </div>
    </>
  );
}
