# mesproject-springboot

멀티모듈(Multi-module) MES 이벤트 기반 샘플 프로젝트입니다.

## Modules
- `libs:event-contract` : Kafka 토픽/이벤트 DTO 모음
- `services:rfid-gateway` : RFID 수신 API → Kafka `rfid.raw`(예시)
- `services:event-router` : Kafka Streams 정규화/중복제거 → `rfid.normalized`
- `services:mes-core` : WIP 처리(JPA+Flyway) + Outbox → `mes.domain.events`
- `services:integration-hub` : ERP 인바운드 API + MES 이벤트 수집

## Infra (Docker)
```powershell
cd .\docker
docker compose up -d
```

Kafka UI: http://localhost:8088

## Run (Windows/PowerShell)

```powershell
cd ..
.\gradlew.bat :services:mes-core:bootRun
```

새 터미널에서:

```powershell
cd ..
.\gradlew.bat :services:integration-hub:bootRun
```

## Quick Test

### ERP 작업지시 업서트 (integration-hub)
```powershell
curl.exe -X POST "http://127.0.0.1:8083/erp/work-orders" ^
  -H "Content-Type: application/json" ^
  -H "X-Request-Id: erp-req-001" ^
  -d "{\"woNo\":\"WO-10001\",\"itemCode\":\"ITEM-ABC\",\"quantity\":100,\"dueDate\":\"2025-12-31\"}"
```

> 동일한 `X-Request-Id`로 재전송하면 idempotency 처리로 DUPLICATE 응답이 나올 수 있습니다.

# MES 확장 기능 패키지 (이식용)

이 ZIP에는 아래 기능이 **코드로 구현**되어 포함되어 있습니다.

## 1) 사용자 1만명+ 대비 인증/권한(RBAC)
- JWT 로그인: `POST /auth/login`
- 내 정보: `GET /auth/me`
- 관리자 전용 사용자 관리:
    - `GET /admin/users`
    - `POST /admin/users` (ADMIN/USER 생성)
    - `POST /admin/users/{id}/enable` (활성/비활성)
    - `POST /admin/users/{id}/role` (권한 변경)
- 초기 Admin 자동 생성(첫 기동 시, 사용자 0명일 때만 생성)
    - 기본값: `admin / admin1234`
    - 환경변수/프로퍼티:
        - `security.bootstrap-admin.username`
        - `security.bootstrap-admin.password`
        - `security.bootstrap-admin.enabled`

## 2) 실시간 로그 관리(대시보드용)
- mes-core / integration-hub에 Logback Kafka Appender 추가
- 로그 토픽: `mes.logs`
- mes-core에서 Kafka consumer로 수신 후 SSE로 브로드캐스트:
    - `GET /admin/logs/stream` (SSE)
- 프론트(관리자): 실시간 로그 페이지 제공

## 3) TPS/APM(요약)
- Actuator + Micrometer Prometheus 노출
    - `GET /actuator/prometheus`
- 관리자 요약 API:
    - `GET /admin/metrics/summary`

## 4) 현장 Telemetry(온도/습도/전력)
- 센서 인입(키 기반):
    - `POST /telemetry/ingest` (Header: `X-API-KEY`)
- Kafka 토픽: `site.telemetry.raw`
- mes-core consumer가 DB 저장 + SSE 이벤트 송출(eventName=telemetry)
- 관리자 조회:
    - `GET /admin/telemetry/recent?siteId=SITE-01&limit=200`
    - `GET /admin/telemetry/range?siteId=...&from=...&to=...`

## 5) 실적 데이터(50만건 이상 조회 대비)
- 테이블: `production_result`
- 키셋 페이징 조회:
    - `GET /admin/results?limit=100&cursorCreatedAt=...&cursorId=...`
    - `GET /app/results?limit=50&cursorCreatedAt=...&cursorId=...`
- Kafka 기반 적재(옵션):
    - 토픽: `production.result`
    - 테스트 인입: `POST /admin/results/ingest`

## DB / 마이그레이션
- `mes-core/src/main/resources/db/migration`
    - `V2__auth_rbac.sql`
    - `V3__telemetry.sql`
    - `V4__production_results.sql`

## Kafka 토픽
- infra compose에 추가:
    - `mes.logs`
    - `site.telemetry.raw`
    - `production.result`

> 주의: 서비스 구성/프로파일(docker/local)에 따라 포트, 프록시, DB 설정은 프로젝트 환경에 맞게 조정하세요.
