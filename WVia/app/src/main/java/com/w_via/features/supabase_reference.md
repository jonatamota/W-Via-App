# Supabase — Schema Real & RLS (entrega temporária)

> Arquivos de referência do lado do Supabase. NÃO são parte do APK — mantenha aqui
> apenas enquanto a implementação não migra para uma pasta `supabase/` oficial.
>
> O schema abaixo é o REAL fornecido pelo usuário (contexto do banco). Os scripts
> de RLS/índices assumem que essas tabelas já existem.

## 1. Schema real (referência — NÃO executar)

```sql
-- Tabela espelho do auth (linha criada por trigger em auth.users).
-- O app resolve o `id` (BIGINT) a partir do `user_id` (UUID):
--   SELECT id FROM users WHERE user_id = <uuid>
CREATE TABLE public.users (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  user_id uuid NOT NULL,
  display_name text,
  avatar_url text,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone DEFAULT now(),
  CONSTRAINT users_pkey PRIMARY KEY (id),
  CONSTRAINT users_user_id_fkey FOREIGN KEY (user_id) REFERENCES auth.users(id)
);

-- Dados pessoais. `phone` é BIGINT no servidor; o app mantém a String formatada
-- apenas para exibição/PDF e converte para número na gravação.
CREATE TABLE public.personal_data (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  user_id bigint,
  birthday date,
  phone bigint,
  sex text,
  CONSTRAINT personal_data_pkey PRIMARY KEY (id),
  CONSTRAINT personal_data_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);

-- Dados profissionais. `image_logo` guarda o caminho no bucket "W-Via-images".
-- `city`/`state` são enviados pelo app desde a versão com autocomplete de locais
-- (cidade/UF da empresa separadas do endereço completo).
CREATE TABLE public.professional_data (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  user_id bigint,
  company_name text,
  email_prof text,
  phone_prof bigint,
  cnpj text,
  address text,
  image_logo text,
  city text,
  state text,
  CONSTRAINT professional_data_pkey PRIMARY KEY (id),
  CONSTRAINT professional_data_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);

-- Estado do cadastro (upsert com onConflict = "user_id", 1 linha por usuário).
CREATE TABLE public.status_registration (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  user_id bigint,
  personal_complete boolean DEFAULT false,
  professional_complete boolean DEFAULT false,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone DEFAULT now(),
  CONSTRAINT status_registration_pkey PRIMARY KEY (id),
  CONSTRAINT status_registration_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);
```

## 2. Índices únicos — REQUERIDO pelo upsert do app

As 3 tabelas de cadastro NÃO possuem constraint `unique` em `user_id`, mas o app
faz `upsert ... onConflict = "user_id"`. Sem um índice único a operação falha.
Execute este bloco:

```sql
create unique index if not exists personal_data_user_id_key
    on public.personal_data (user_id);

create unique index if not exists professional_data_user_id_key
    on public.professional_data (user_id);

create unique index if not exists status_registration_user_id_key
    on public.status_registration (user_id);
```

> Se já existirem linhas duplicadas, remova as duplicadas antes de criar os índices.

## 3. RLS — entrega 1 (políticas por usuário autenticado)

```sql
alter table public.users enable row level security;
alter table public.personal_data enable row level security;
alter table public.professional_data enable row level security;
alter table public.status_registration enable row level security;

drop policy if exists "users_select_own" on public.users;
create policy "users_select_own" on public.users
    for select using (auth.uid() = user_id);

drop policy if exists "users_update_own" on public.users;
create policy "users_update_own" on public.users
    for update using (auth.uid() = user_id) with check (auth.uid() = user_id);

-- personal_data
drop policy if exists "personal_data_select_own" on public.personal_data;
create policy "personal_data_select_own" on public.personal_data
    for select using (auth.uid() = (select u.user_id from public.users u where u.id = personal_data.user_id));

drop policy if exists "personal_data_insert_own" on public.personal_data;
create policy "personal_data_insert_own" on public.personal_data
    for insert with check (auth.uid() = (select u.user_id from public.users u where u.id = personal_data.user_id));

drop policy if exists "personal_data_update_own" on public.personal_data;
create policy "personal_data_update_own" on public.personal_data
    for update using (auth.uid() = (select u.user_id from public.users u where u.id = personal_data.user_id))
    with check (auth.uid() = (select u.user_id from public.users u where u.id = personal_data.user_id));

-- professional_data
drop policy if exists "professional_data_select_own" on public.professional_data;
create policy "professional_data_select_own" on public.professional_data
    for select using (auth.uid() = (select u.user_id from public.users u where u.id = professional_data.user_id));

drop policy if exists "professional_data_insert_own" on public.professional_data;
create policy "professional_data_insert_own" on public.professional_data
    for insert with check (auth.uid() = (select u.user_id from public.users u where u.id = professional_data.user_id));

drop policy if exists "professional_data_update_own" on public.professional_data;
create policy "professional_data_update_own" on public.professional_data
    for update using (auth.uid() = (select u.user_id from public.users u where u.id = professional_data.user_id))
    with check (auth.uid() = (select u.user_id from public.users u where u.id = professional_data.user_id));

-- status_registration
drop policy if exists "status_registration_select_own" on public.status_registration;
create policy "status_registration_select_own" on public.status_registration
    for select using (auth.uid() = (select u.user_id from public.users u where u.id = status_registration.user_id));

drop policy if exists "status_registration_insert_own" on public.status_registration;
create policy "status_registration_insert_own" on public.status_registration
    for insert with check (auth.uid() = (select u.user_id from public.users u where u.id = status_registration.user_id));

drop policy if exists "status_registration_update_own" on public.status_registration;
create policy "status_registration_update_own" on public.status_registration
    for update using (auth.uid() = (select u.user_id from public.users u where u.id = status_registration.user_id))
    with check (auth.uid() = (select u.user_id from public.users u where u.id = status_registration.user_id));
```

## 4. Grants (o app usa a chave anon)

```sql
grant usage on schema public to anon, authenticated;
grant select, insert, update on all tables in schema public to anon, authenticated;
grant select, insert, update, delete on all tables in schema public to service_role;
```

## 5. Correção do `receipt.user_id` (FK aponta para tabela errada)

No schema real, `receipt_user_id_fkey` referencia `auth.oauth_clients(id)`, mas o
app grava o **UUID do usuário autenticado** (`AuthManager.getCurrentUserId()`),
que vem de `auth.users`. Essa FK está errada e quebrará o push dos recibos quando
a sync real for implementada. Corrija com:

```sql
-- 1. Remove a FK incorreta
alter table public.receipt
    drop constraint if exists receipt_user_id_fkey;

-- 2. Recria apontando para auth.users(id) — o id que o app envia
alter table public.receipt
    add constraint receipt_user_id_fkey
    foreign key (user_id) references auth.users (id)
    on delete set null;
```

> Como o app ainda usa sync placeholder (nenhum push real), não é urgente, mas
> evita rework na integração. RLS do `receipt` pode seguir o mesmo padrão das
> demais tabelas (comparar `auth.uid() = user_id`), já que aqui o `user_id`
> é o UUID — mais simples que o join via `users`.

## 6. MIGRAÇÃO OBRIGATÓRIA — colunas `city`/`state` em `professional_data`

O app grava `city` e `state` ao salvar os dados profissionais (tela "Informe o
seu CNPJ"). Sem essas colunas o POST / upsert falha com
`PGRST204: Could not find the 'city' column of ...` — e, até a correção do app,
a falha era silenciosa (o botão "Continuar" só mostrava o spinner e voltava).

Se o seu banco ainda não tem as colunas (schema antigo), execute no SQL editor
do Supabase:

```sql
alter table public.professional_data add column if not exists city text;
alter table public.professional_data add column if not exists state text;
```

> `ADD COLUMN IF NOT EXISTS` é idempotente: pode rodar mais de uma vez sem erro.
> Após executar, refaça o teste "Continuar" na tela do CNPJ — a gravação passa a
> funcionar e o app navega para a HOME.
