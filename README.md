# SK hynix 스타일 MES 공정 자동화 · 무인 공정 체크 시스템 (Service Package B)

이 패키지는 **실제 시연/공유(사내 데모·포트폴리오·고객 시연)**를 목적으로, 한 번의 Docker Compose로 전체 스택을 올릴 수 있게 구성했습니다.

## 포함 구성
- **Nginx**: Operator/Kiosk UI 정적 서빙 + API Reverse Proxy
- **Operator UI**: WIP 모니터링, 알람 센터(ACK/ASSIGN/CLOSE), 성과표(KPI) + 엑셀 다운로드
- **Kiosk UI**: 스테이션 선택, 태그 입력(스캔) IN/OUT
- **Spring Boot(멀티모듈)**
  - `mes-core`: WIP/알람/SLA/KPI/Excel + SSE
  - `integration-hub`: ERP 인바운드 + 인터페이스 메시지
  - `rfid-gateway`: RFID raw 수신(선택)
  - `event-router`: Kafka Streams 정규화(선택)
- **Postgres**: 서비스 데이터 저장
- **Kafka + Zookeeper + Kafka-UI**: 이벤트 파이프라인 + UI

---

## 0) 요구사항
- Docker Desktop (Windows/macOS) 또는 Docker Engine (Linux)
- (권장) 8GB+ RAM

---

## 1) 실행 (한 방)
패키지 루트에서:

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

### 접속 URL
- **Operator**: `http://localhost/operator/`
- **Kiosk**: `http://localhost/kiosk/`
- **Kafka UI**: `http://localhost:${KAFKA_UI_PORT:-8088}`
- **Core Health**: `http://localhost/health/core`
- **Hub Health**: `http://localhost/health/hub`

### 기본 계정
- Admin: `admin / admin1234`

> 계정/비밀번호는 `.env`에서 변경하세요.

---

## 2) 시연 시나리오(추천)
1. Operator 로그인
2. **Admin → System Master**: 라인/스테이션 등록
3. **Admin → Process SLA**: 스테이션별 maxCycle/maxStuck/manual/auto/ideal/manning 설정
4. Kiosk에서 스테이션 선택 → Tag 입력(스캔) → **IN/OUT** 반복
5. Operator에서
   - **WIP Monitor**: 실시간 흐름(SSE)
   - **Alarm Center**: CYCLE_EXCEEDED / STUCK 자동 알람 생성 → ACK/ASSIGN/CLOSE
   - **Performance**: KPI 카드 + 엑셀 다운로드

---

## 3) 다른 사람에게 보여주기(동일 LAN)
- 같은 네트워크에서 다른 PC/모바일은 `http://<서버IP>/operator/` 로 접속 가능합니다.
- 방화벽에서 80 포트(또는 `.env`의 HTTP_PORT) 오픈 필요할 수 있습니다.

---

## 4) 중지/정리
```bash
docker compose -f docker-compose.prod.yml down
```

데이터까지 초기화(주의):
```bash
docker compose -f docker-compose.prod.yml down -v
```

---

## 5) Kafka/Gradle 캐시 이슈 관련
- 이 패키지는 Docker 빌드로 일관되게 빌드하기 때문에 로컬 Gradle 캐시 꼬임 영향을 덜 받습니다.
- 그래도 빌드 단계에서 Kafka 의존성 충돌이 나면 `.env`나 Docker 캐시를 초기화 후 재빌드하세요.

```bash
docker builder prune -af
docker compose -f docker-compose.prod.yml build --no-cache
```

---

## 6) 커스터마이징 포인트
- `.env`:
  - Admin 계정/비밀번호
  - 외부 노출 포트 변경
- `deploy/nginx/nginx.conf`:
  - Reverse Proxy 경로/타임아웃/SSE 설정


---

## 3-1) 외부 공개 가능한 서비스(HTTPS: Reverse Proxy + 인증서)
이 프로젝트는 **Caddy**를 앞단 Reverse Proxy로 두어 **Let's Encrypt 자동 인증서**로 HTTPS를 구성할 수 있습니다.

### 준비
- 도메인 `DOMAIN`의 A 레코드가 서버 공인 IP를 가리키도록 설정
- 방화벽/보안그룹에서 **80, 443** 포트 오픈
- `.env` 또는 환경변수에 아래 값 세팅
  - `DOMAIN=yourdomain.com`
  - `ACME_EMAIL=you@yourdomain.com`

### 실행
```bash
docker compose -f docker-compose.prod.yml -f docker-compose.public.yml up -d --build
```

### 접속
- Operator: `https://<DOMAIN>/operator/`
- Kiosk: `https://<DOMAIN>/kiosk/`

> NOTE: 로컬/내부만 쓸 때는 기존처럼 `docker-compose.prod.yml`만 올리면 됩니다.

---

## 7) 운영자 KPI 고도화 (MTTA/MTTR + 담당자 성과 + 병목 Heatmap)
Operator UI의 **Performance** 화면에서 아래가 추가로 제공됩니다.
- 알람 대응 리포트: **Avg/P95 MTTA(Detect→ACK)**, **Avg/P95 MTTR(Detect→Close)**
- 담당자별 성과표: Assigned/Acked/Closed + 평균/95퍼센타일 MTTA/MTTR
- 라인/스테이션 병목 **Heatmap** (점수 높을수록 병목)
- Excel 다운로드: Summary/Station Metrics/Assignee Performance/Alarms(Window)
