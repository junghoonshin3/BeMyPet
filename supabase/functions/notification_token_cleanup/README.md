# notification_token_cleanup

Removes stale or invalid FCM tokens from `notification_subscriptions`.

## Request

`POST /functions/v1/notification_token_cleanup`

Service invoker only:
- `Authorization: Bearer <service_role_key or service_role_jwt>`
- `apikey: <service_role_key>`

### Mode: stale

```json
{
  "mode": "stale",
  "stale_before_days": 30,
  "user_ids": ["uuid-for-test-scope"],
  "dry_run": false
}
```

- Deletes rows where `coalesce(last_active_at, updated_at, created_at)` is older than cutoff.
- `stale_before_days` default is `30`.
- `user_ids` is optional and can scope cleanup to specific users (useful for smoke tests).

### Mode: invalid

```json
{
  "mode": "invalid",
  "invalid_tokens": ["token_a", "token_b"],
  "dry_run": false
}
```

- Deletes rows whose `fcm_token` is included in `invalid_tokens`.

## Response

```json
{
  "mode": "stale",
  "dry_run": false,
  "matched_count": 2,
  "deleted_count": 2
}
```

## Local Example

```bash
curl -sS -X POST "$SUPABASE_URL/functions/v1/notification_token_cleanup" \
  -H "apikey: $SUPABASE_SERVICE_ROLE_KEY" \
  -H "Authorization: Bearer $SUPABASE_SERVICE_ROLE_KEY" \
  -H "Content-Type: application/json" \
  --data '{"mode":"stale","stale_before_days":30,"dry_run":true}'
```
