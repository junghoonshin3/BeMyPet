import { createClient } from "jsr:@supabase/supabase-js@2";

type CleanupMode = "stale" | "invalid";

type CleanupRequest = {
  mode?: CleanupMode;
  stale_before_days?: number;
  invalid_tokens?: string[];
  user_ids?: string[];
  dry_run?: boolean;
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

function parseIsoMs(value: string | null | undefined): number | null {
  if (!value) return null;
  const ts = Date.parse(value);
  return Number.isFinite(ts) ? ts : null;
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

  const isServiceInvoker = bearer === serviceRoleKey || parseJwtRole(bearer) === "service_role";
  if (!isServiceInvoker) {
    return json({ error: "Unauthorized" }, 401);
  }

  let payload: CleanupRequest = {};
  try {
    payload = await req.json();
  } catch {
    payload = {};
  }

  const mode = payload.mode;
  const dryRun = payload.dry_run ?? true;
  const staleBeforeDays = Math.max(1, Math.floor(payload.stale_before_days ?? 30));
  const filterUserIds = Array.from(
    new Set((payload.user_ids ?? []).map((x) => x.trim()).filter((x) => x.length > 0)),
  );

  const adminClient = createClient(supabaseUrl, serviceRoleKey, {
    auth: {
      autoRefreshToken: false,
      persistSession: false,
    },
  });

  if (mode === "stale") {
    const cutoffMs = Date.now() - staleBeforeDays * 24 * 60 * 60 * 1000;
    let query = adminClient
      .from("notification_subscriptions")
      .select("id, user_id, fcm_token, last_active_at, updated_at, created_at");
    if (filterUserIds.length > 0) {
      query = query.in("user_id", filterUserIds);
    }
    const { data, error } = await query;

    if (error) {
      return json(
        {
          error: "Failed to query subscriptions",
          details: error.message,
        },
        500,
      );
    }

    const staleRows = (data ?? []).filter((row) => {
      const lastActive = parseIsoMs(row.last_active_at);
      const updatedAt = parseIsoMs(row.updated_at);
      const createdAt = parseIsoMs(row.created_at);
      const lastSeenMs = lastActive ?? updatedAt ?? createdAt;
      return typeof lastSeenMs === "number" && lastSeenMs <= cutoffMs;
    });

    if (!dryRun && staleRows.length > 0) {
      const ids = staleRows.map((row) => row.id).filter((id): id is string => typeof id === "string" && id.length > 0);
      if (ids.length > 0) {
        const { error: deleteError } = await adminClient
          .from("notification_subscriptions")
          .delete()
          .in("id", ids);
        if (deleteError) {
          return json(
            {
              error: "Failed to delete stale subscriptions",
              details: deleteError.message,
            },
            500,
          );
        }
      }
    }

    return json({
      mode: "stale",
      dry_run: dryRun,
      stale_before_days: staleBeforeDays,
      user_filter_count: filterUserIds.length,
      matched_count: staleRows.length,
      deleted_count: dryRun ? 0 : staleRows.length,
    });
  }

  if (mode === "invalid") {
    const invalidTokens = Array.from(
      new Set(
        (payload.invalid_tokens ?? [])
          .map((token) => token.trim())
          .filter((token) => token.length > 0),
      ),
    );

    if (invalidTokens.length === 0) {
      return json({ error: "invalid_tokens required for invalid mode" }, 400);
    }

    const { data, error } = await adminClient
      .from("notification_subscriptions")
      .select("id, fcm_token")
      .in("fcm_token", invalidTokens);

    if (error) {
      return json(
        {
          error: "Failed to query invalid token rows",
          details: error.message,
        },
        500,
      );
    }

    const matchedRows = data ?? [];

    if (!dryRun && matchedRows.length > 0) {
      const ids = matchedRows.map((row) => row.id).filter((id): id is string => typeof id === "string" && id.length > 0);
      if (ids.length > 0) {
        const { error: deleteError } = await adminClient
          .from("notification_subscriptions")
          .delete()
          .in("id", ids);
        if (deleteError) {
          return json(
            {
              error: "Failed to delete invalid token rows",
              details: deleteError.message,
            },
            500,
          );
        }
      }
    }

    return json({
      mode: "invalid",
      dry_run: dryRun,
      matched_count: matchedRows.length,
      deleted_count: dryRun ? 0 : matchedRows.length,
    });
  }

  return json({ error: "mode must be one of: stale, invalid" }, 400);
});
