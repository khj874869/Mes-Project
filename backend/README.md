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

