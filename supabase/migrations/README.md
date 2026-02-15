# Supabase Migration Notes

Apply migrations in timestamp order.

1. `20260216_profiles_comments_auth_refactor.sql`

This migration introduces:
- `profiles` table as app profile source
- `comments.notice_no` standardization
- `comment_feed` / `block_feed` views
- RLS policies for profiles/comments/blocks/reports
- profile-image storage bucket and policies

## Edge Function follow-up

`banned_until` and `delete_user` are currently called as Supabase Edge Functions from Android.
If those functions exist, update them to be compatible with soft-delete (`profiles.is_deleted`, `profiles.deleted_at`).
