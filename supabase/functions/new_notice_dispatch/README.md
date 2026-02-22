# new_notice_dispatch

Matches `notification_subscriptions` rows with `push_opt_in=true` and writes queued rows to `notification_delivery_logs`.

## Request

`POST /functions/v1/new_notice_dispatch`

```json
{
  "dry_run": true,
  "notices": [{ "notice_no": "A2026-0001" }]
}
```

## Response

```json
{
  "matched_users": 12,
  "queued": 0,
  "dry_run": true
}
```

## Deploy

```bash
supabase functions deploy new_notice_dispatch
```
