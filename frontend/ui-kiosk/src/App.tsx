import { Link, Route, Routes, NavLink } from "react-router-dom";
import StationSelect from "./pages/StationSelect";
import ScanScreen from "./pages/ScanScreen";

function Nav() {
  return (
    <div className="nav">
      <div className="nav-inner">
        <div className="row">
          <strong>MES Kiosk</strong>
          <span className="pill muted">core :8080</span>
        </div>
        <div className="nav-links">
          <NavLink className="pill" to="/">스테이션 선택</NavLink>
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
          <Route path="/" element={<StationSelect />} />
          <Route path="/station/:stationCode" element={<ScanScreen />} />
          <Route path="*" element={<div className="card">Not found. <Link to="/">Go home</Link></div>} />
        </Routes>
      </div>
    </>
  );
}
