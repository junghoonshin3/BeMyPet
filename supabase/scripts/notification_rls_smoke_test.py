import json
import pathlib
import re
import subprocess
import time
import urllib.error
import urllib.parse
import urllib.request


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


def ok(name: str):
    print(f"PASS  {name}")


def bad(name: str, msg: str):
    print(f"FAIL  {name}: {msg}")


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

    # Fallback: parse project ref from secrets.dev.properties SUPABASE_URL
    secrets_file = root / "secrets.dev.properties"
    if secrets_file.exists():
        text = secrets_file.read_text(encoding="utf-8")
        match = re.search(r"SUPABASE_URL\s*=\s*https://([^.]+)\.supabase\.co", text)
        if match:
            return match.group(1)

    raise RuntimeError("could not resolve Supabase project ref from .temp or secrets.dev.properties")


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
    anon_item = next((x for x in keys if x.get("name") == "anon"), None)
    service_item = next((x for x in keys if x.get("name") == "service_role"), None)
    if not anon_item or not service_item:
        raise SystemExit("Could not find anon/service_role keys from supabase CLI output")

    anon_key = pick_key(anon_item)
    service_key = pick_key(service_item)
    if not anon_key or not service_key:
        raise SystemExit("Could not parse key material from supabase CLI output")

    def rest_url(path_and_query: str) -> str:
        return base_url + path_and_query

    ts = int(time.time())
    pw = f"Test_{ts}_pw!123"

    def admin_create(email: str, name: str):
        url = rest_url("/auth/v1/admin/users")
        status, body = http_json(
            "POST",
            url,
            headers={
                "apikey": service_key,
                "Authorization": f"Bearer {service_key}",
                "Accept": "application/json",
            },
            payload={
                "email": email,
                "password": pw,
                "email_confirm": True,
                "user_metadata": {"name": name},
            },
        )
        if status not in (200, 201):
            raise RuntimeError(f"admin_create failed: {status} {body}")
        if isinstance(body, dict) and "id" in body:
            return body["id"]
        if (
            isinstance(body, dict)
            and "user" in body
            and isinstance(body["user"], dict)
            and "id" in body["user"]
        ):
            return body["user"]["id"]
        raise RuntimeError(f"admin_create: unexpected body {body}")

    def admin_delete(uid: str):
        url = rest_url(f"/auth/v1/admin/users/{urllib.parse.quote(uid, safe='')}")
        status, _ = http_json(
            "DELETE",
            url,
            headers={
                "apikey": service_key,
                "Authorization": f"Bearer {service_key}",
                "Accept": "application/json",
            },
        )
        return status

    def login(email: str):
        url = rest_url("/auth/v1/token?grant_type=password")
        status, body = http_json(
            "POST",
            url,
            headers={"apikey": anon_key, "Accept": "application/json"},
            payload={"email": email, "password": pw},
        )
        if status != 200:
            raise RuntimeError(f"login failed: {status} {body}")
        return body["access_token"], body["user"]["id"]

    def rest_headers(token: str, prefer_return: bool = False):
        h = {
            "apikey": anon_key,
            "Authorization": f"Bearer {token}",
            "Accept": "application/json",
        }
        if prefer_return:
            h["Prefer"] = "return=representation"
        return h

    a_email = f"notif_rls_a_{ts}@example.com"
    b_email = f"notif_rls_b_{ts}@example.com"
    user_ids: list[str] = []

    try:
        admin_create(a_email, "NotifA")
        admin_create(b_email, "NotifB")
        a_token, a_uid = login(a_email)
        b_token, b_uid = login(b_email)
        user_ids = [a_uid, b_uid]
        ok("auth admin create + password login")

        # Failing-first for pre-migration: this should 404/42P01 before table creation.
        check_url = rest_url("/rest/v1/user_interest_profiles?select=user_id&limit=1")
        st, body = http_json("GET", check_url, headers=rest_headers(a_token))
        if st == 200:
            ok("user_interest_profiles relation exists")
        else:
            bad("user_interest_profiles relation exists", f"status={st} body={body}")
            raise SystemExit(1)

        interest_url = rest_url("/rest/v1/user_interest_profiles")
        st, body = http_json(
            "POST",
            interest_url,
            headers=rest_headers(a_token, prefer_return=True),
            payload={
                "user_id": a_uid,
                "regions": ["6110000"],
                "species": ["dog"],
                "sexes": ["M"],
                "sizes": ["SMALL"],
                "push_enabled": True,
            },
        )
        if st in (200, 201):
            ok("interest profile insert self")
        else:
            bad("interest profile insert self", f"status={st} body={body}")
            raise SystemExit(1)

        st, body = http_json(
            "POST",
            interest_url,
            headers=rest_headers(b_token, prefer_return=True),
            payload={
                "user_id": a_uid,
                "regions": ["6260000"],
            },
        )
        if st >= 400:
            ok("interest profile spoof blocked")
        else:
            bad("interest profile spoof blocked", f"unexpected success status={st} body={body}")

        sub_url = rest_url("/rest/v1/notification_subscriptions")
        st, body = http_json(
            "POST",
            sub_url,
            headers=rest_headers(a_token, prefer_return=True),
            payload={
                "user_id": a_uid,
                "fcm_token": f"tok_{ts}_a",
                "push_opt_in": True,
                "timezone": "Asia/Seoul",
            },
        )
        if st in (200, 201):
            ok("subscription insert self")
        else:
            bad("subscription insert self", f"status={st} body={body}")
            raise SystemExit(1)

        st, body = http_json(
            "GET",
            rest_url(f"/rest/v1/notification_subscriptions?select=user_id&user_id=eq.{urllib.parse.quote(a_uid, safe='')}") ,
            headers=rest_headers(b_token),
        )
        if st == 200 and isinstance(body, list) and len(body) == 0:
            ok("subscription cross-user select blocked")
        else:
            bad("subscription cross-user select blocked", f"status={st} body={body}")

        st, body = http_json(
            "PATCH",
            rest_url(f"/rest/v1/notification_subscriptions?user_id=eq.{urllib.parse.quote(a_uid, safe='')}") ,
            headers=rest_headers(b_token, prefer_return=True),
            payload={"push_opt_in": False},
        )
        if st >= 400 or (st in (200, 204) and (body is None or body == [])):
            ok("subscription cross-user update blocked")
        else:
            bad("subscription cross-user update blocked", f"status={st} body={body}")

        # service role inserts logs to verify user scoped reads on notification_delivery_logs.
        log_url = rest_url("/rest/v1/notification_delivery_logs")
        st, body = http_json(
            "POST",
            log_url,
            headers={
                "apikey": service_key,
                "Authorization": f"Bearer {service_key}",
                "Accept": "application/json",
                "Prefer": "return=representation",
            },
            payload={
                "user_id": a_uid,
                "campaign_type": "new_animal",
                "notice_no": f"N{ts}",
                "dedupe_key": f"dedupe_{ts}_a",
                "status": "queued",
                "payload_json": {"source": "test"},
            },
        )
        if st in (200, 201):
            ok("delivery log insert by service role")
        else:
            bad("delivery log insert by service role", f"status={st} body={body}")
            raise SystemExit(1)

        st, body = http_json(
            "GET",
            rest_url(f"/rest/v1/notification_delivery_logs?select=user_id,dedupe_key&user_id=eq.{urllib.parse.quote(a_uid, safe='')}") ,
            headers=rest_headers(a_token),
        )
        if st == 200 and isinstance(body, list) and len(body) >= 1:
            ok("delivery log select self")
        else:
            bad("delivery log select self", f"status={st} body={body}")

        st, body = http_json(
            "GET",
            rest_url(f"/rest/v1/notification_delivery_logs?select=user_id,dedupe_key&user_id=eq.{urllib.parse.quote(a_uid, safe='')}") ,
            headers=rest_headers(b_token),
        )
        if st == 200 and isinstance(body, list) and len(body) == 0:
            ok("delivery log cross-user select blocked")
        else:
            bad("delivery log cross-user select blocked", f"status={st} body={body}")

    finally:
        for uid in user_ids:
            status = admin_delete(uid)
            if status in (200, 204):
                print("CLEAN", uid[:8])
            else:
                print("CLEAN_FAIL", uid[:8], status)


if __name__ == "__main__":
    main()
