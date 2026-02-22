# Supabase Migration Notes

Apply migrations in timestamp order.

1. `20260216_profiles_comments_auth_refactor.sql`
2. `20260224_add_notification_retention_tables.sql`

This migration introduces:
- `profiles` table as app profile source
- `comments.notice_no` standardization
- `comment_feed` / `block_feed` views
- RLS policies for profiles/comments/blocks/reports
- profile-image storage bucket and policies

`20260224_add_notification_retention_tables.sql` introduces:
- `user_interest_profiles` table (new user preference targeting)
- `notification_subscriptions` table (device token + opt-in + delivery throttle state)
- `notification_delivery_logs` table (dedupe + delivery/open audit)
- RLS policies for self-scoped read/write where appropriate

Smoke test:
- `python3 supabase/scripts/notification_rls_smoke_test.py`

## Edge Function follow-up

`banned_until` and `delete_user` are currently called as Supabase Edge Functions from Android.
If those functions exist, update them to be compatible with soft-delete (`profiles.is_deleted`, `profiles.deleted_at`).
