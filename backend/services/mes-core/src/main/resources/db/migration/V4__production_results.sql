create table if not exists production_result (
  id bigserial primary key,
  work_order_no varchar(60) not null,
  line_code varchar(40) not null,
  station_code varchar(40),
  item_code varchar(60) not null,
  qty_good int not null default 0,
  qty_ng int not null default 0,
  started_at timestamptz,
  ended_at timestamptz,
  created_at timestamptz not null default now()
);

create index if not exists idx_prod_created_id on production_result(created_at desc, id desc);
create index if not exists idx_prod_work_order on production_result(work_order_no);
create index if not exists idx_prod_line_created on production_result(line_code, created_at desc);
