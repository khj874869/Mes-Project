CREATE TABLE IF NOT EXISTS processed_event (
  id BIGSERIAL PRIMARY KEY,
  idempotency_key VARCHAR(200) NOT NULL UNIQUE,
  processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS wip_unit (
  id BIGSERIAL PRIMARY KEY,
  tag_id VARCHAR(100) NOT NULL UNIQUE,
  serial_no VARCHAR(100) NOT NULL,
  last_station VARCHAR(50),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS wip_event (
  id BIGSERIAL PRIMARY KEY,
  event_id VARCHAR(100) NOT NULL,
  idempotency_key VARCHAR(200) NOT NULL,
  tag_id VARCHAR(100) NOT NULL,
  station_code VARCHAR(50) NOT NULL,
  direction VARCHAR(10),
  occurred_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_wip_event_tag_id ON wip_event(tag_id);
CREATE INDEX IF NOT EXISTS idx_wip_event_idem ON wip_event(idempotency_key);

CREATE TABLE IF NOT EXISTS outbox_event (
  id BIGSERIAL PRIMARY KEY,
  event_type VARCHAR(100) NOT NULL,
  aggregate_id VARCHAR(100) NOT NULL,
  payload_json TEXT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'NEW',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  published_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_outbox_status ON outbox_event(status);

CREATE TABLE IF NOT EXISTS work_order (
  id BIGSERIAL PRIMARY KEY,
  wo_no VARCHAR(50) NOT NULL UNIQUE,
  item_code VARCHAR(50) NOT NULL,
  quantity INT NOT NULL,
  due_date DATE,
  status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
