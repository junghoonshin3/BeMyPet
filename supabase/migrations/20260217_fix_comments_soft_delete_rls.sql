-- Fix: Allow soft delete (setting deleted_at) under RLS.
--
-- PostgreSQL requires updated rows to satisfy SELECT policies as well. If a SELECT policy filters
-- out rows where deleted_at is not null, then updating deleted_at will be rejected by RLS.
-- We keep deleted comments hidden from other users, but allow the author/admin to see them.

alter policy comments_select_visible on public.comments
using (
  auth.role() = 'authenticated'
  and (
    deleted_at is null
    or auth.uid() = user_id
    or public.is_admin(auth.uid())
  )
  and not exists (
    select 1
    from public.blocks b
    where b.blocker_id = auth.uid()
      and b.blocked_id = comments.user_id
  )
);

