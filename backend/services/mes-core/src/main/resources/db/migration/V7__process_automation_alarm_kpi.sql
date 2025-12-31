-- 공정 자동화(무인 체크) + 알람 + KPI 기반 데이터

CREATE TABLE IF NOT EXISTS station_sla (
  station_code VARCHAR(20) PRIMARY KEY,
  max_cycle_sec INTEGER NOT NULL DEFAULT 300,
  max_stuck_sec INTEGER NOT NULL DEFAULT 600,
  manual_check_sec INTEGER NOT NULL DEFAULT 30,
  auto_check_sec INTEGER NOT NULL DEFAULT 5,
  ideal_cycle_sec INTEGER NOT NULL DEFAULT 120,
  manning INTEGER NOT NULL DEFAULT 1,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_station_sla_station'
  ) THEN
    ALTER TABLE station_sla
      ADD CONSTRAINT fk_station_sla_station
      FOREIGN KEY(station_code) REFERENCES mes_station(station_code)
      ON UPDATE CASCADE ON DELETE RESTRICT;
  END IF;
END $$;

-- 기존 스테이션에 대한 기본값 시드
INSERT INTO station_sla(station_code, max_cycle_sec, max_stuck_sec, manual_check_sec, auto_check_sec, ideal_cycle_sec, manning)
SELECT station_code, 300, 600, 30, 5, 120, 1
FROM mes_station
ON CONFLICT (station_code) DO NOTHING;

CREATE TABLE IF NOT EXISTS alarm_event (
  id BIGSERIAL PRIMARY KEY,
  alarm_type VARCHAR(30) NOT NULL,
  severity VARCHAR(10) NOT NULL DEFAULT 'WARN',
  status VARCHAR(10) NOT NULL DEFAULT 'OPEN',
  tag_id VARCHAR(100) NOT NULL,
  station_code VARCHAR(20) NOT NULL,
  line_code VARCHAR(20),
  message TEXT,
  occurred_at TIMESTAMPTZ,
  detected_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  acked_at TIMESTAMPTZ,
  closed_at TIMESTAMPTZ,
  assigned_to VARCHAR(80),
  last_updated_by VARCHAR(80),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_alarm_status ON alarm_event(status, detected_at DESC);
CREATE INDEX IF NOT EXISTS idx_alarm_station ON alarm_event(station_code, detected_at DESC);
CREATE INDEX IF NOT EXISTS idx_alarm_tag ON alarm_event(tag_id, detected_at DESC);

-- OPEN/ACK 상태에서는 동일 유형 알람을 중복 생성하지 않도록 부분 유니크 인덱스
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_indexes WHERE indexname = 'uk_alarm_open'
  ) THEN
    CREATE UNIQUE INDEX uk_alarm_open
      ON alarm_event(alarm_type, tag_id, station_code)
      WHERE status IN ('OPEN','ACK');
  END IF;
END $$;

CREATE TABLE IF NOT EXISTS alarm_history (
  id BIGSERIAL PRIMARY KEY,
  alarm_id BIGINT NOT NULL,
  action VARCHAR(20) NOT NULL,
  actor VARCHAR(80),
  note TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_alarm_history_alarm'
  ) THEN
    ALTER TABLE alarm_history
      ADD CONSTRAINT fk_alarm_history_alarm
      FOREIGN KEY(alarm_id) REFERENCES alarm_event(id)
      ON UPDATE CASCADE ON DELETE CASCADE;
  END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_alarm_history_alarm ON alarm_history(alarm_id, created_at DESC);
