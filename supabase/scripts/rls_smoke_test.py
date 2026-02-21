import json
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
    # CLI json schema is not stable across versions; try common fields.
    for k in ("key", "key_value", "value", "api_key"):
        if k in item and isinstance(item[k], str) and item[k]:
            return item[k]
    # Some outputs nest key in 'data'
    if "data" in item and isinstance(item["data"], dict):
        for k in ("key", "key_value", "value", "api_key"):
            v = item["data"].get(k)
            if isinstance(v, str) and v:
                return v
    return None


def main():
    project_ref = "lvmcycuhrgqgdmxwdnmy"
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

    a_email = f"rls_a_{ts}@example.com"
    b_email = f"rls_b_{ts}@example.com"
    user_ids: list[str] = []

    try:
        admin_create(a_email, "RlsA")
        admin_create(b_email, "RlsB")
        a_token, a_uid = login(a_email)
        b_token, b_uid = login(b_email)
        user_ids = [a_uid, b_uid]
        ok("auth admin create + password login")

        def fetch_profile(token: str, uid: str):
            q = urllib.parse.quote(uid, safe="")
            url = rest_url(
                f"/rest/v1/profiles?select=user_id,display_name,avatar_url,is_deleted&user_id=eq.{q}"
            )
            return http_json("GET", url, headers=rest_headers(token))

        for (token, uid, label) in [(a_token, a_uid, "A"), (b_token, b_uid, "B")]:
            for _ in range(6):
                st, body = fetch_profile(token, uid)
                if st == 200 and isinstance(body, list) and len(body) == 1:
                    break
                time.sleep(0.5)
            if (
                st == 200
                and isinstance(body, list)
                and len(body) == 1
                and body[0].get("is_deleted") is False
            ):
                ok(f"profiles auto-create ({label})")
            else:
                bad(f"profiles auto-create ({label})", f"status={st} body={body}")

        notice = f"notice_{ts}"

        comments_url = rest_url("/rest/v1/comments")
        st, body = http_json(
            "POST",
            comments_url,
            headers=rest_headers(a_token, prefer_return=True),
            payload={"notice_no": notice, "user_id": a_uid, "content": "hello from A"},
        )
        if st in (200, 201) and isinstance(body, list) and body:
            a_comment_id = body[0]["id"]
            ok("comments insert self (A)")
        else:
            bad("comments insert self (A)", f"status={st} body={body}")
            raise SystemExit(1)

        # Update own comment content (should succeed)
        patch_url = rest_url(
            f"/rest/v1/comments?id=eq.{urllib.parse.quote(a_comment_id, safe='')}"
        )
        st, body = http_json(
            "PATCH",
            patch_url,
            headers=rest_headers(a_token, prefer_return=True),
            payload={"content": "edited by A"},
        )
        if st in (200, 204):
            ok("comments update self")
        else:
            bad("comments update self", f"status={st} body={body}")

        st, body = http_json(
            "POST",
            comments_url,
            headers=rest_headers(b_token, prefer_return=True),
            payload={"notice_no": notice, "user_id": b_uid, "content": "hello from B"},
        )
        if st in (200, 201) and isinstance(body, list) and body:
            b_comment_id = body[0]["id"]
            ok("comments insert self (B)")
        else:
            bad("comments insert self (B)", f"status={st} body={body}")
            raise SystemExit(1)

        st, body = http_json(
            "POST",
            comments_url,
            headers=rest_headers(a_token, prefer_return=True),
            payload={"notice_no": notice, "user_id": b_uid, "content": "spoof"},
        )
        if st >= 400:
            ok("comments insert spoof user_id blocked")
        else:
            bad(
                "comments insert spoof user_id blocked",
                f"unexpected success status={st} body={body}",
            )

        feed_url = rest_url(
            "/rest/v1/comment_feed"
            f"?select=id,notice_no,user_id,content,author_name,author_avatar_url,author_deleted,created_at"
            f"&notice_no=eq.{urllib.parse.quote(notice, safe='')}"
            "&order=created_at.asc"
        )
        st, body = http_json("GET", feed_url, headers=rest_headers(a_token))
        if st == 200 and isinstance(body, list) and len(body) >= 2:
            ok("comment_feed visible before block")
        else:
            bad("comment_feed visible before block", f"status={st} body={body}")

        blocks_url = rest_url("/rest/v1/blocks")
        st, body = http_json(
            "POST",
            blocks_url,
            headers=rest_headers(a_token, prefer_return=True),
            payload={"blocker_id": a_uid, "blocked_id": b_uid},
        )
        if st in (200, 201):
            ok("blocks insert self")
        else:
            bad("blocks insert self", f"status={st} body={body}")

        st, body = http_json("GET", feed_url, headers=rest_headers(a_token))
        if st == 200 and isinstance(body, list) and all(
            r.get("user_id") == a_uid for r in body
        ):
            ok("comment_feed hides blocked user's comments")
        else:
            bad("comment_feed hides blocked user's comments", f"status={st} body={body}")

        st, body = http_json("GET", feed_url, headers=rest_headers(b_token))
        if st == 200 and isinstance(body, list) and any(
            r.get("user_id") == a_uid for r in body
        ):
            ok("comment_feed not affected for non-blocker")
        else:
            bad("comment_feed not affected for non-blocker", f"status={st} body={body}")

        patch_url = rest_url(
            f"/rest/v1/comments?id=eq.{urllib.parse.quote(b_comment_id, safe='')}"
        )
        st, body = http_json(
            "PATCH",
            patch_url,
            headers=rest_headers(a_token, prefer_return=True),
            payload={"content": "hacked"},
        )
        # PostgREST returns 200 with empty list when RLS filters out all rows to update.
        if st >= 400 or (st in (200, 204) and (body is None or body == [])):
            ok("comments update other blocked")
        else:
            bad(
                "comments update other blocked",
                f"unexpected success status={st} body={body}",
            )

        patch_url = rest_url(
            f"/rest/v1/comments?id=eq.{urllib.parse.quote(a_comment_id, safe='')}"
        )
        st, body = http_json(
            "PATCH",
            patch_url,
            # Don't request representation; deleted rows won't be selectable by policy.
            headers=rest_headers(a_token, prefer_return=False),
            payload={"deleted_at": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime())},
        )
        if st in (200, 204):
            ok("comments soft delete self")
        else:
            bad("comments soft delete self", f"status={st} body={body}")

        st, body = http_json("GET", feed_url, headers=rest_headers(a_token))
        if st == 200 and isinstance(body, list) and all(r.get("user_id") != a_uid for r in body):
            ok("comment_feed excludes deleted comments")
        else:
            bad("comment_feed excludes deleted comments", f"status={st} body={body}")

        prof_url = rest_url(
            f"/rest/v1/profiles?user_id=eq.{urllib.parse.quote(a_uid, safe='')}"
        )
        st, body = http_json(
            "PATCH",
            prof_url,
            headers=rest_headers(a_token, prefer_return=True),
            payload={"display_name": f"RlsA_{ts}"},
        )
        if st in (200, 204):
            ok("profiles update self")
        else:
            bad("profiles update self", f"status={st} body={body}")

        st, body = http_json(
            "PATCH",
            prof_url,
            headers=rest_headers(a_token, prefer_return=True),
            payload={"display_name": f"RlsA2_{ts}"},
        )
        if st >= 400:
            ok("profiles nickname cooldown enforced")
        else:
            bad(
                "profiles nickname cooldown enforced",
                f"unexpected success status={st} body={body}",
            )

    finally:
        for uid in user_ids:
            status = admin_delete(uid)
            if status in (200, 204):
                print("CLEAN", uid[:8])
            else:
                print("CLEAN_FAIL", uid[:8], status)


if __name__ == "__main__":
    main()
