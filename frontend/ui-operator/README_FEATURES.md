# UI 확장 기능 (이식용)

## dev proxy (Vite)
- `/api/core/*` -> `http://localhost:8080/*`
- `/api/hub/*`  -> `http://localhost:8083/*`

## 로그인
- 기본 계정(백엔드 첫 기동 시 자동 생성): `admin / admin1234`

## 관리자 메뉴
- Users (RBAC)
- Logs (SSE 실시간)
- Telemetry (SSE + DB recent)
- TPS/APM 요약
- Results (키셋 페이징)

> 백엔드가 다른 포트/도메인이라면 `vite.config.ts`의 proxy만 수정하면 됩니다.
