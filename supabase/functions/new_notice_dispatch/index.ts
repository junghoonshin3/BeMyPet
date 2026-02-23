import { createClient } from "jsr:@supabase/supabase-js@2";
import { buildDateWindow } from "./dispatch_core.ts";

type DispatchNotice = {
  notice_no?: string;
};

type DispatchRequest = {
  dry_run?: boolean;
  notices?: DispatchNotice[];
};

function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

function extractBearerToken(authHeader: string | null): string {
  if (!authHeader || !authHeader.startsWith("Bearer ")) return "";
  return authHeader.substring(7).trim();
}

function parseJwtRole(token: string): string | null {
  try {
    const payloadPart = token.split(".")[1];
    if (!payloadPart) return null;
    const b64 = payloadPart.replace(/-/g, "+").replace(/_/g, "/");
    const padded = b64 + "=".repeat((4 - (b64.length % 4)) % 4);
    const payload = JSON.parse(atob(padded));
    return typeof payload?.role === "string" ? payload.role : null;
  } catch {
    return null;
  }
}

Deno.serve(async (req) => {
  if (req.method !== "POST") {
    return json({ error: "Method not allowed" }, 405);
  }

  const supabaseUrl = Deno.env.get("SUPABASE_URL");
  const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  if (!supabaseUrl || !serviceRoleKey) {
    return json({ error: "Missing server configuration" }, 500);
  }

  const bearer = extractBearerToken(req.headers.get("Authorization"));
  if (!bearer) {
    return json({ error: "Unauthorized" }, 401);
  }

  const adminClient = createClient(supabaseUrl, serviceRoleKey, {
    auth: {
      autoRefreshToken: false,
      persistSession: false,
    },
  });

  const isServiceInvoker = bearer === serviceRoleKey || parseJwtRole(bearer) === "service_role";
  if (!isServiceInvoker) {
    const { data: authData, error: authError } = await adminClient.auth.getUser(bearer);
    if (authError || !authData.user) {
      return json({ error: "Unauthorized" }, 401);
    }
  }

  let payload: DispatchRequest = {};
  try {
    payload = await req.json();
  } catch {
    payload = {};
  }

  const dryRun = payload.dry_run ?? false;
  const notices = Array.isArray(payload.notices) ? payload.notices : [];
  const normalizedNoticeNo = notices[0]?.notice_no?.trim() || null;
  const runWindow = buildDateWindow(null, new Date().toISOString().slice(0, 10));

  const { data: subscriptions, error: subscriptionError } = await adminClient
    .from("notification_subscriptions")
    .select("user_id, fcm_token, push_opt_in")
    .eq("push_opt_in", true);

  if (subscriptionError) {
    return json(
      {
        error: "Failed to query subscriptions",
        details: subscriptionError.message,
      },
      500,
    );
  }

  const matched = subscriptions ?? [];

  if (dryRun) {
    return json({
      matched_users: matched.length,
      queued: 0,
      dry_run: true,
      window: runWindow,
    });
  }

  let queued = 0;
  const errors: Array<{ user_id: string; message: string }> = [];

  for (const sub of matched) {
    const noticeNo = normalizedNoticeNo ?? "unknown";
    const dedupeKey = `new_animal:${sub.user_id}:${noticeNo}`;

    const { error } = await adminClient.from("notification_delivery_logs").upsert(
      {
        user_id: sub.user_id,
        campaign_type: "new_animal",
        notice_no: normalizedNoticeNo,
        dedupe_key: dedupeKey,
        status: "queued",
        payload_json: {
          dry_run: false,
          notice_no: normalizedNoticeNo,
          token_preview: String(sub.fcm_token).slice(0, 12),
        },
        sent_at: new Date().toISOString(),
      },
      { onConflict: "dedupe_key" },
    );

    if (error) {
      errors.push({ user_id: String(sub.user_id), message: error.message });
      continue;
    }

    queued += 1;
  }

  return json({
    matched_users: matched.length,
    queued,
    dry_run: false,
    window: runWindow,
    errors_count: errors.length,
    errors,
  });
});
