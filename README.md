# MES Project (Monorepo)

이 저장소는 **백엔드(Spring Boot 멀티모듈)** + **프론트엔드(운영자 대시보드/현장 키오스크)** 를 함께 관리합니다.  
Windows + Docker Desktop 환경에서 바로 실행/테스트할 수 있도록 구성했습니다.

## 구조

```
frontend/              # React(Vite) UI
  ui-operator/         # 운영자 대시보드
  ui-kiosk/            # 현장 키오스크

springboot/            # Backend (Gradle multi-module)
  services/
    mes-core/          # MES Core API (8080)
    integration-hub/   # ERP ↔ MES 허브 (8083)
    rfid-gateway/      # RFID 수신 (8081)
    event-router/      # Kafka Streams (8082)
  libs/
    event-contract/    # 이벤트 DTO/토픽 정의
  docker/              # Postgres + Kafka + Kafka UI
```

## 사전 준비물

- Java 17 (권장: Corretto 17)
- Docker Desktop
- Node.js LTS

## 빠른 실행

### 1) 인프라 실행 (Postgres/Kafka/Kafka UI)

PowerShell:

```powershell
cd .\springboot\docker
docker compose up -d
docker ps
```

Kafka UI: http://localhost:8088

### 2) 백엔드 실행

```powershell
cd .\springboot
.\gradlew.bat :services:mes-core:bootRun
```

새 터미널에서:

```powershell
cd .\springboot
.\gradlew.bat :services:integration-hub:bootRun
```

헬스 체크:

```powershell
curl.exe http://127.0.0.1:8080/actuator/health
curl.exe http://127.0.0.1:8083/actuator/health
```

### 3) 프론트 실행

운영자 대시보드:

```powershell
cd .\frontend\ui-operator
npm install
npm run dev
```

키오스크:

```powershell
cd .\frontend\ui-kiosk
npm install
npm run dev
```

## Flyway 주의사항 (checksum mismatch)

이미 적용된 마이그레이션 SQL(V1__*.sql)을 수정하면 Flyway 체크섬 오류가 발생할 수 있습니다.

- 로컬 Docker DB를 초기화해도 되면:

```powershell
cd .\springboot\docker
docker compose down -v
docker compose up -d
```

- DB를 유지해야 한다면: 기존 V1은 원복하고, 변경분은 **V2__...sql**로 추가하세요.
