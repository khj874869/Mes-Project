create table if not exists telemetry_reading (
  id bigserial primary key,
  site_id varchar(50) not null,
  sensor_id varchar(80) not null,
  metric varchar(20) not null, -- TEMP, HUMIDITY, POWER
  value double precision not null,
  ts timestamptz not null
);

create index if not exists idx_telemetry_site_ts on telemetry_reading(site_id, ts desc);
create index if not exists idx_telemetry_metric_ts on telemetry_reading(metric, ts desc);
create index if not exists idx_telemetry_site_metric_ts on telemetry_reading(site_id, metric, ts desc);
