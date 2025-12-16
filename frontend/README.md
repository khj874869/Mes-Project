# MES Frontend (Operator Dashboard + Kiosk)

Two Vite + React + TypeScript apps:

- `ui-operator` (operator dashboard) — proxy to integration-hub (8083)
- `ui-kiosk` (kiosk UI) — proxy to mes-core (8080)

## Run (PowerShell)

### Operator
```powershell
cd frontend\ui-operator
npm install
npm run dev
```
Open: http://localhost:5173

### Kiosk
```powershell
cd ..\ui-kiosk
npm install
npm run dev
```
Open: http://localhost:5174
