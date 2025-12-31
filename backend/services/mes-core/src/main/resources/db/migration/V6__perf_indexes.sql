-- WIP 조회/타임라인 성능 개선

CREATE INDEX IF NOT EXISTS idx_wip_unit_last_station ON wip_unit(last_station);

CREATE INDEX IF NOT EXISTS idx_wip_event_occurred_id ON wip_event(occurred_at DESC, id DESC);
CREATE INDEX IF NOT EXISTS idx_wip_event_station_occurred ON wip_event(station_code, occurred_at DESC);

-- outbox 스캐줄러 폴링 성능 (status 기준)
CREATE INDEX IF NOT EXISTS idx_outbox_status_id ON outbox_event(status, id);
