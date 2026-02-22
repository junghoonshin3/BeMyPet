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
    pw = f"Dispatch_{ts}_pw!123"
    email = f"dispatch_{ts}@example.com"
    user_id = None

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
                "user_metadata": {"name": "DispatchUser"},
            },
        )
        if status not in (200, 201):
            raise RuntimeError(f"admin create failed: {status} {body}")

        if isinstance(body, dict) and "id" in body:
            user_id = body["id"]
        elif (
            isinstance(body, dict)
            and isinstance(body.get("user"), dict)
            and body["user"].get("id")
        ):
            user_id = body["user"]["id"]
        else:
            raise RuntimeError(f"unexpected admin create body: {body}")

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
                "fcm_token": f"dispatch_token_{ts}",
                "push_opt_in": True,
                "timezone": "Asia/Seoul",
            },
        )
        if status not in (200, 201):
            raise RuntimeError(f"insert subscription failed: {status} {body}")

        status, body = http_json(
            "POST",
            rest_url("/functions/v1/new_notice_dispatch"),
            headers={
                "apikey": service_key,
                "Authorization": f"Bearer {service_key}",
            },
            payload={"dry_run": True, "notices": [{"notice_no": f"notice_{ts}"}]},
        )

        if status != 200:
            raise RuntimeError(f"dispatch function failed: {status} {body}")

        if not isinstance(body, dict) or not isinstance(body.get("matched_users"), int):
            raise RuntimeError(f"invalid response payload: {body}")

        print("PASS  new_notice_dispatch dry-run")
        print(f"INFO  matched_users={body.get('matched_users')}")

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
