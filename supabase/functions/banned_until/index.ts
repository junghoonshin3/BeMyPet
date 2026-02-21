import { createClient } from "jsr:@supabase/supabase-js@2";

type BanResponse = {
  isBanned: boolean;
  bannedUntil: string;
};

const DEFAULT_RESPONSE: BanResponse = {
  isBanned: false,
  bannedUntil: "알수없음",
};

function json(body: BanResponse, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      "Content-Type": "application/json",
    },
  });
}

function isMissingBannedColumn(error: {
  code?: string;
  message?: string;
  details?: string | null;
  hint?: string | null;
}) {
  const raw = `${error.code ?? ""} ${error.message ?? ""} ${error.details ?? ""} ${error.hint ?? ""}`.toLowerCase();
  return raw.includes("banned_until") && raw.includes("column") && raw.includes("exist");
}

Deno.serve(async (req) => {
  if (req.method !== "POST") {
    return json(DEFAULT_RESPONSE, 405);
  }

  const supabaseUrl = Deno.env.get("SUPABASE_URL");
  const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");

  if (!supabaseUrl || !serviceRoleKey) {
    console.error("Missing required env: SUPABASE_URL or SUPABASE_SERVICE_ROLE_KEY");
    return json(DEFAULT_RESPONSE);
  }

  const authHeader = req.headers.get("Authorization") ?? "";
  const bearer = authHeader.startsWith("Bearer ") ? authHeader.substring(7).trim() : "";
  if (!bearer) {
    return json(DEFAULT_RESPONSE, 401);
  }

  const adminClient = createClient(supabaseUrl, serviceRoleKey, {
    auth: {
      autoRefreshToken: false,
      persistSession: false,
    },
  });

  const { data: authData, error: authError } = await adminClient.auth.getUser(bearer);
  if (authError || !authData.user) {
    console.warn("Auth verification failed in banned_until");
    return json(DEFAULT_RESPONSE, 401);
  }

  let requestedUserId = authData.user.id;
  try {
    const payload = await req.json();
    if (typeof payload?.user_id === "string" && payload.user_id.length > 0) {
      requestedUserId = payload.user_id;
    }
  } catch {
    // Ignore malformed body and fallback to authenticated user id.
  }

  // Prevent querying other users unless caller is the same user.
  if (requestedUserId !== authData.user.id) {
    requestedUserId = authData.user.id;
  }

  const { data, error } = await adminClient
    .from("profiles")
    .select("banned_until")
    .eq("user_id", requestedUserId)
    .maybeSingle();

  if (error) {
    if (isMissingBannedColumn(error)) {
      console.warn("profiles.banned_until column is missing. Returning not banned.");
      return json(DEFAULT_RESPONSE);
    }

    console.error("Failed to fetch banned_until", {
      code: error.code,
      message: error.message,
    });
    return json(DEFAULT_RESPONSE);
  }

  const bannedUntilRaw = (data as { banned_until: string | null } | null)?.banned_until;
  if (!bannedUntilRaw) {
    return json(DEFAULT_RESPONSE);
  }

  const bannedUntilDate = new Date(bannedUntilRaw);
  if (Number.isNaN(bannedUntilDate.getTime())) {
    return json(DEFAULT_RESPONSE);
  }

  const isBanned = bannedUntilDate.getTime() > Date.now();
  return json({
    isBanned,
    bannedUntil: isBanned ? bannedUntilRaw : DEFAULT_RESPONSE.bannedUntil,
  });
});
