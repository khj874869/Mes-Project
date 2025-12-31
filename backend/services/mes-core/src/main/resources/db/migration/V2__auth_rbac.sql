create table if not exists app_user (
  id bigserial primary key,
  username varchar(60) not null unique,
  password_hash varchar(120) not null,
  display_name varchar(80) not null,
  enabled boolean not null default true,
  created_at timestamptz not null default now()
);

create table if not exists app_role (
  id bigserial primary key,
  name varchar(30) not null unique  -- ROLE_ADMIN, ROLE_USER
);

create table if not exists app_user_role (
  user_id bigint not null references app_user(id) on delete cascade,
  role_id bigint not null references app_role(id) on delete cascade,
  primary key (user_id, role_id)
);

insert into app_role(name) values ('ROLE_ADMIN') on conflict do nothing;
insert into app_role(name) values ('ROLE_USER')  on conflict do nothing;
