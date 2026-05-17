-- Kreeda Ankana — Supabase schema
--
-- Run this once in your Supabase project (SQL Editor → New query).
-- The app authenticates anonymously via the anon key only and identifies
-- each phone with a locally-generated device_id. RLS policies below match
-- that "open village notice board" model: anyone with the anon key can
-- read everything and post new entries, but row ownership is recorded so
-- you can later tighten policies (e.g. "only owner can update/delete")
-- when an auth flow is added.

create extension if not exists pgcrypto;

-- ---- bookings ----
create table if not exists public.bookings (
  id           uuid        primary key default gen_random_uuid(),
  team_name    text        not null,
  sport        text        not null,                  -- encoded id|displayName|emoji
  date         text        not null,                  -- ISO date "YYYY-MM-DD"
  hour         int         not null check (hour between 0 and 23),
  device_id    text        not null,
  created_at   bigint      not null,
  unique (date, hour)
);

-- ---- challenges ----
create table if not exists public.challenges (
  id              uuid        primary key default gen_random_uuid(),
  team_name       text        not null,
  sport           text        not null,
  preferred_date  text,
  preferred_hour  int         check (preferred_hour is null or preferred_hour between 0 and 23),
  note            text,
  status          text        not null default 'OPEN',
  accepted_by     text,
  accepted_at     bigint,
  device_id       text        not null,
  created_at      bigint      not null
);

-- ---- scores ----
create table if not exists public.scores (
  id           uuid        primary key default gen_random_uuid(),
  team_a       text        not null,
  team_b       text        not null,
  score_a      int         not null,
  score_b      int         not null,
  sport        text        not null,
  date         text        not null,
  note         text,
  device_id    text        not null,
  created_at   bigint      not null
);

create index if not exists scores_created_at_idx on public.scores (created_at desc);
create index if not exists challenges_status_idx  on public.challenges (status, created_at desc);

-- ---- RLS: open notice board ----
alter table public.bookings   enable row level security;
alter table public.challenges enable row level security;
alter table public.scores     enable row level security;

-- bookings: read all, insert any. (No update/delete from clients.)
drop policy if exists "bookings_read_all"   on public.bookings;
drop policy if exists "bookings_insert_any" on public.bookings;
create policy "bookings_read_all"   on public.bookings for select using (true);
create policy "bookings_insert_any" on public.bookings for insert with check (true);

-- challenges: read all, insert any, update any (so other teams can mark accepted).
drop policy if exists "challenges_read_all"   on public.challenges;
drop policy if exists "challenges_insert_any" on public.challenges;
drop policy if exists "challenges_update_any" on public.challenges;
create policy "challenges_read_all"   on public.challenges for select using (true);
create policy "challenges_insert_any" on public.challenges for insert with check (true);
create policy "challenges_update_any" on public.challenges for update using (true);

-- scores: read all, insert any.
drop policy if exists "scores_read_all"   on public.scores;
drop policy if exists "scores_insert_any" on public.scores;
create policy "scores_read_all"   on public.scores for select using (true);
create policy "scores_insert_any" on public.scores for insert with check (true);
