CREATE TABLE IF NOT EXISTS interface_message (
  id BIGSERIAL PRIMARY KEY,
  idempotency_key VARCHAR(200) NOT NULL UNIQUE,
  direction VARCHAR(20) NOT NULL,
  system_name VARCHAR(30) NOT NULL,
  message_type VARCHAR(50) NOT NULL,
  payload_json TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_if_msg_direction ON interface_message(direction);
