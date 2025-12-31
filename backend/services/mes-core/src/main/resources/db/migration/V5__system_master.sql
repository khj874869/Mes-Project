CREATE TABLE IF NOT EXISTS mes_line (
  id BIGSERIAL PRIMARY KEY,
  line_code VARCHAR(20) NOT NULL UNIQUE,
  name VARCHAR(60) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS mes_station (
  id BIGSERIAL PRIMARY KEY,
  station_code VARCHAR(20) NOT NULL UNIQUE,
  name VARCHAR(60) NOT NULL,
  line_code VARCHAR(20) NOT NULL,
  seq INTEGER NOT NULL,
  kiosk_enabled BOOLEAN NOT NULL DEFAULT TRUE,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_station_line'
  ) THEN
    ALTER TABLE mes_station
      ADD CONSTRAINT fk_station_line
      FOREIGN KEY(line_code) REFERENCES mes_line(line_code)
      ON UPDATE CASCADE ON DELETE RESTRICT;
  END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_station_line_seq ON mes_station(line_code, seq);
CREATE INDEX IF NOT EXISTS idx_station_active_kiosk ON mes_station(active, kiosk_enabled);

-- ===== seed (demo) =====
INSERT INTO mes_line(line_code, name, active)
SELECT 'L01', 'Main Line', TRUE
WHERE NOT EXISTS (SELECT 1 FROM mes_line);

INSERT INTO mes_station(station_code, name, line_code, seq, kiosk_enabled, active)
SELECT * FROM (
  VALUES
    ('S01', '입고/투입', 'L01', 10, TRUE, TRUE),
    ('S02', '조립',     'L01', 20, TRUE, TRUE),
    ('S03', '검사',     'L01', 30, TRUE, TRUE),
    ('S04', '포장',     'L01', 40, TRUE, TRUE)
) AS v(station_code, name, line_code, seq, kiosk_enabled, active)
WHERE NOT EXISTS (SELECT 1 FROM mes_station);
