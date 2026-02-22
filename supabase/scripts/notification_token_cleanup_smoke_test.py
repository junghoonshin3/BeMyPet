import json
import pathlib
import re
import subprocess
import time
import urllib.error
import urllib.parse
import urllib.request
from datetime import datetime, timedelta, timezone


def sh_json(cmd: list[str]) -> object:
    out = subprocess.check_output(cmd)
    return json.loads(out.decode("utf-8"))


def http_json(method: str, url: str, headers: dict[str, str], payload=None):
    data = None
    if payload is not None:
        data = json.dumps(payload).encode("utf-8")
        headers = {**headers, "Content-Type": "application/json"}
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            body = resp.read().decode("utf-8")
            if body.strip() == "":
                return resp.status, None
            return resp.status, json.loads(body)
    except urllib.error.HTTPError as e:
        raw = e.read().decode("utf-8")
        try:
            return e.code, json.loads(raw) if raw.strip() else None
        except Exception:
            return e.code, {"raw": raw}


def pick_key(item: dict) -> str | None:
    for k in ("key", "key_value", "value", "api_key"):
        if k in item and isinstance(item[k], str) and item[k]:
            return item[k]
    if "data" in item and isinstance(item["data"], dict):
        for k in ("key", "key_value", "value", "api_key"):
            v = item["data"].get(k)
            if isinstance(v, str) and v:
                return v
    return None


def read_project_ref() -> str:
    root = pathlib.Path(__file__).resolve().parents[2]
    candidates = [
        root / "supabase" / ".temp" / "project-ref",
        root / "supabase" / "supabase" / ".temp" / "project-ref",
    ]
    for ref_file in candidates:
        if ref_file.exists():
            raw = ref_file.read_text(encoding="utf-8").strip()
            if raw:
                return raw

    secrets_file = root / "secrets.dev.properties"
    if secrets_file.exists():
        text = secrets_file.read_text(encoding="utf-8")
        match = re.search(r"SUPABASE_URL\s*=\s*https://([^.]+)\.supabase\.co", text)
        if match:
            return match.group(1)

    raise RuntimeError("could not resolve Supabase project ref")


def ok(name: str):
    print(f"PASS  {name}")


def fail(name: str, msg: str):
    raise RuntimeError(f"{name}: {msg}")


def iso_utc(days_ago: int) -> str:
    return (datetime.now(tz=timezone.utc) - timedelta(days=days_ago)).isoformat()


def main():
    project_ref = read_project_ref()
    base_url = f"https://{project_ref}.supabase.co"

    keys = sh_json(
        [
            "supabase",
            "projects",
            "api-keys",
            "--project-ref",
            project_ref,
            "-o",
            "json",
        ]
    )
    service_item = next((x for x in keys if x.get("name") == "service_role"), None)
    if not service_item:
        raise SystemExit("Could not find service_role key")
    service_key = pick_key(service_item)
    if not service_key:
        raise SystemExit("Could not parse service_role key")

    def rest_url(path_and_query: str) -> str:
        return base_url + path_and_query

    ts = int(time.time())
    pw = f"Cleanup_{ts}_pw!123"
    email = f"cleanup_{ts}@example.com"
    user_id = None
    stale_token = f"cleanup_stale_{ts}"
    fresh_token = f"cleanup_fresh_{ts}"
    invalid_token = f"cleanup_invalid_{ts}"

    try:
        status, body = http_json(
            "POST",
            rest_url("/auth/v1/admin/users"),
            headers={
                "apikey": service_key,
                "Authorization": f"Bearer {service_key}",
                "Accept": "application/json",
            },
            payload={
                "email": email,
                "password": pw,
                "email_confirm": True,
                "user_metadata": {"name": "CleanupUser"},
            },
        )
        if status not in (200, 201):
            fail("admin create", f"status={status} body={body}")

        if isinstance(body, dict) and "id" in body:
            user_id = body["id"]
        elif (
            isinstance(body, dict)
            and isinstance(body.get("user"), dict)
            and body["user"].get("id")
        ):
            user_id = body["user"]["id"]
        else:
            fail("admin create", f"unexpected body {body}")

        for token, days in ((stale_token, 31), (fresh_token, 1), (invalid_token, 1)):
            status, body = http_json(
                "POST",
                rest_url("/rest/v1/notification_subscriptions"),
                headers={
                    "apikey": service_key,
                    "Authorization": f"Bearer {service_key}",
                    "Accept": "application/json",
                    "Prefer": "return=representation",
                },
                payload={
                    "user_id": user_id,
                    "fcm_token": token,
                    "push_opt_in": True,
                    "timezone": "Asia/Seoul",
                    "last_active_at": iso_utc(days),
                },
            )
            if status not in (200, 201):
                fail("seed subscriptions", f"token={token} status={status} body={body}")
        ok("seed subscriptions")

        status, body = http_json(
            "POST",
            rest_url("/functions/v1/notification_token_cleanup"),
            headers={
                "apikey": service_key,
                "Authorization": f"Bearer {service_key}",
            },
            payload={
                "mode": "stale",
                "stale_before_days": 30,
                "user_ids": [user_id],
                "dry_run": True,
            },
        )
        if status != 200:
            fail("stale dry-run", f"status={status} body={body}")
        if not isinstance(body, dict) or body.get("deleted_count") != 0:
            fail("stale dry-run", f"unexpected body={body}")
        if int(body.get("matched_count", 0)) < 1:
            fail("stale dry-run", f"expected matched_count>=1 body={body}")
        ok("stale dry-run")

        status, body = http_json(
            "POST",
            rest_url("/functions/v1/notification_token_cleanup"),
            headers={
                "apikey": service_key,
                "Authorization": f"Bearer {service_key}",
            },
            payload={
                "mode": "stale",
                "stale_before_days": 30,
                "user_ids": [user_id],
                "dry_run": False,
            },
        )
        if status != 200:
            fail("stale delete", f"status={status} body={body}")
        if int(body.get("deleted_count", 0)) < 1:
            fail("stale delete", f"expected deleted_count>=1 body={body}")
        ok("stale delete")

        status, body = http_json(
            "GET",
            rest_url(
                f"/rest/v1/notification_subscriptions?select=fcm_token&fcm_token=eq.{urllib.parse.quote(stale_token, safe='')}"
            ),
            headers={
                "apikey": service_key,
                "Authorization": f"Bearer {service_key}",
                "Accept": "application/json",
            },
        )
        if status != 200 or not isinstance(body, list):
            fail("stale token verify", f"status={status} body={body}")
        if len(body) != 0:
            fail("stale token verify", f"stale token still exists body={body}")
        ok("stale token removed")

        status, body = http_json(
            "POST",
            rest_url("/functions/v1/notification_token_cleanup"),
            headers={
                "apikey": service_key,
                "Authorization": f"Bearer {service_key}",
            },
            payload={"mode": "invalid", "invalid_tokens": [invalid_token], "dry_run": True},
        )
        if status != 200:
            fail("invalid dry-run", f"status={status} body={body}")
        if int(body.get("matched_count", 0)) < 1 or int(body.get("deleted_count", 0)) != 0:
            fail("invalid dry-run", f"unexpected body={body}")
        ok("invalid dry-run")

        status, body = http_json(
            "POST",
            rest_url("/functions/v1/notification_token_cleanup"),
            headers={
                "apikey": service_key,
                "Authorization": f"Bearer {service_key}",
            },
            payload={"mode": "invalid", "invalid_tokens": [invalid_token], "dry_run": False},
        )
        if status != 200:
            fail("invalid delete", f"status={status} body={body}")
        if int(body.get("deleted_count", 0)) < 1:
            fail("invalid delete", f"expected deleted_count>=1 body={body}")
        ok("invalid delete")

        status, body = http_json(
            "GET",
            rest_url(
                f"/rest/v1/notification_subscriptions?select=fcm_token&fcm_token=eq.{urllib.parse.quote(invalid_token, safe='')}"
            ),
            headers={
                "apikey": service_key,
                "Authorization": f"Bearer {service_key}",
                "Accept": "application/json",
            },
        )
        if status != 200 or not isinstance(body, list):
            fail("invalid token verify", f"status={status} body={body}")
        if len(body) != 0:
            fail("invalid token verify", f"invalid token still exists body={body}")
        ok("invalid token removed")

        status, body = http_json(
            "GET",
            rest_url(
                f"/rest/v1/notification_subscriptions?select=fcm_token&fcm_token=eq.{urllib.parse.quote(fresh_token, safe='')}"
            ),
            headers={
                "apikey": service_key,
                "Authorization": f"Bearer {service_key}",
                "Accept": "application/json",
            },
        )
        if status != 200 or not isinstance(body, list):
            fail("fresh token verify", f"status={status} body={body}")
        if len(body) != 1:
            fail("fresh token verify", f"fresh token should remain body={body}")
        ok("fresh token preserved")

    finally:
        if user_id:
            status, _ = http_json(
                "DELETE",
                rest_url(f"/auth/v1/admin/users/{urllib.parse.quote(user_id, safe='')}"),
                headers={
                    "apikey": service_key,
                    "Authorization": f"Bearer {service_key}",
                    "Accept": "application/json",
                },
            )
            print("CLEAN", user_id[:8], status)


if __name__ == "__main__":
    main()
