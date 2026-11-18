-- ============================================================
-- Sincronização local (Room) <-> Supabase — Setup das tabelas
-- `declaracoes` e `recibos`, índices, função de RLS e grants.
--
-- Requisitos:
--   1. Rodar primeiro o DDL base do schema (users, personal_data,
--      professional_data, status_registration) do
--      app/src/main/java/com/w_via/features/supabase_reference.md
--   2. Colar todo este bloco no SQL Editor do Supabase.
--
-- Compatível com a implementação atual do app:
--   - user_id      : BIGINT (id de public.users, via resolveUserId())
--   - created_at/updated_at : BIGINT (epoch millis)
--   - push         : upsert onConflict = "uuid"
--   - exclusão     : soft delete (is_deleted = true), sem DELETE remoto
-- ============================================================

-- ------------------------------------------------------------
-- 1. Tabelas de sincronização
-- ------------------------------------------------------------
create table if not exists public.declaracoes (
  id bigint generated always as identity not null,
  uuid text not null,
  user_id bigint,
  nome text,
  data_obito text,
  local_obito text,
  payload_json text,
  created_at bigint,
  updated_at bigint,
  is_deleted boolean default false,
  constraint declaracoes_pkey primary key (id),
  constraint declaracoes_user_id_fkey foreign key (user_id) references public.users(id)
);

create table if not exists public.recibos (
  id bigint generated always as identity not null,
  uuid text not null,
  user_id bigint,
  nome_contratante text,
  data_pagamento text,
  total_pago text,
  payload_json text,
  created_at bigint,
  updated_at bigint,
  is_deleted boolean default false,
  constraint recibos_pkey primary key (id),
  constraint recibos_user_id_fkey foreign key (user_id) references public.users(id)
);

-- ------------------------------------------------------------
-- 2. Índices
-- ------------------------------------------------------------
-- Índice único em uuid (obrigatório para o upsert onConflict="uuid")
create unique index if not exists declaracoes_uuid_key on public.declaracoes (uuid);
create unique index if not exists recibos_uuid_key on public.recibos (uuid);

-- Lookup do auth.uid() -> users.id usado na função de RLS (uma vez por query)
create unique index if not exists users_user_id_key on public.users (user_id);

-- Filtros do pull (select where user_id = X) e das policies
create index if not exists declaracoes_user_id_idx on public.declaracoes (user_id);
create index if not exists recibos_user_id_idx on public.recibos (user_id);

-- ------------------------------------------------------------
-- 3. Função de RLS: id (bigint) do usuário autenticado
--    SECURITY DEFINER + STABLE => avaliada 1x por statement,
--    evitando a subquery correlacionada por linha.
-- ------------------------------------------------------------
create or replace function public.current_users_id()
returns bigint
language sql
stable
security definer
set search_path = public
as $$
  select u.id
  from public.users u
  where u.user_id = auth.uid()
$$;

-- ------------------------------------------------------------
-- 4. RLS
-- ------------------------------------------------------------
alter table public.declaracoes enable row level security;
alter table public.recibos enable row level security;

drop policy if exists "declaracoes_select_own" on public.declaracoes;
create policy "declaracoes_select_own" on public.declaracoes
  for select using (user_id = public.current_users_id());

drop policy if exists "declaracoes_insert_own" on public.declaracoes;
create policy "declaracoes_insert_own" on public.declaracoes
  for insert with check (user_id = public.current_users_id());

drop policy if exists "declaracoes_update_own" on public.declaracoes;
create policy "declaracoes_update_own" on public.declaracoes
  for update using (user_id = public.current_users_id())
  with check (user_id = public.current_users_id());

drop policy if exists "recibos_select_own" on public.recibos;
create policy "recibos_select_own" on public.recibos
  for select using (user_id = public.current_users_id());

drop policy if exists "recibos_insert_own" on public.recibos;
create policy "recibos_insert_own" on public.recibos
  for insert with check (user_id = public.current_users_id());

drop policy if exists "recibos_update_own" on public.recibos;
create policy "recibos_update_own" on public.recibos
  for update using (user_id = public.current_users_id())
  with check (user_id = public.current_users_id());

-- ------------------------------------------------------------
-- 5. Grants (o app usa a chave anon)
-- ------------------------------------------------------------
grant select, insert, update on public.declaracoes to anon, authenticated;
grant select, insert, update on public.recibos to anon, authenticated;
grant select, insert, update, delete on public.declaracoes to service_role;
grant select, insert, update, delete on public.recibos to service_role;

-- ------------------------------------------------------------
-- 6. Verificação (deve retornar 6 políticas)
-- ------------------------------------------------------------
select tablename, policyname, cmd
from pg_policies
where tablename in ('declaracoes','recibos');
