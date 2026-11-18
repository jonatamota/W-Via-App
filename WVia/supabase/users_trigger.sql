-- ============================================================
-- Cria a linha espelho em `public.users` quando um usuário
-- novo é criado em `auth.users`.
--
-- Sem esse trigger, `resolveUserId()` (app) lança exceção ao
-- fazer SELECT em `public.users` e TODA a sincronização falha.
--
-- Requer o índice único `users_user_id_key` (sync_tables.sql:59).
-- Colar todo este bloco no SQL Editor do Supabase.
-- ============================================================

create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer set search_path = public
as $$
begin
  insert into public.users (user_id, display_name, avatar_url)
  values (new.id, new.raw_user_meta_data->>'full_name', new.raw_user_meta_data->>'avatar_url')
  on conflict (user_id) do nothing;
  return new;
end;
$$;

create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();
