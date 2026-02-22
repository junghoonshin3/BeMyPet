import { createClient } from "jsr:@supabase/supabase-js@2";

type DeleteUserResponse = {
  success: boolean;
  code: string;
  message?: string;
};

function json(body: DeleteUserResponse, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      "Content-Type": "application/json",
    },
  });
}

function extractBearerToken(authHeader: string | null): string {
  if (!authHeader || !authHeader.startsWith("Bearer ")) return "";
  return authHeader.substring(7).trim();
}

function isRelationMissing(error: { code?: string; message?: string }): boolean {
  return error.code === "42P01" || (error.message ?? "").toLowerCase().includes("does not exist");
}

Deno.serve(async (req) => {
  if (req.method !== "POST") {
    return json(
      {
        success: false,
        code: "METHOD_NOT_ALLOWED",
        message: "Method not allowed",
      },
      405
    );
  }

  const supabaseUrl = Deno.env.get("SUPABASE_URL");
  const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  if (!supabaseUrl || !serviceRoleKey) {
    console.error("delete_user failed", {
      code: "SERVER_CONFIG_ERROR",
      message: "Missing required env: SUPABASE_URL or SUPABASE_SERVICE_ROLE_KEY",
    });
    return json(
      {
        success: false,
        code: "SERVER_CONFIG_ERROR",
        message: "Server configuration error",
      },
      500
    );
  }

  const bearer = extractBearerToken(req.headers.get("Authorization"));
  if (!bearer) {
    return json(
      {
        success: false,
        code: "UNAUTHORIZED",
        message: "Unauthorized",
      },
      401
    );
  }

  const adminClient = createClient(supabaseUrl, serviceRoleKey, {
    auth: {
      autoRefreshToken: false,
      persistSession: false,
    },
  });

  const { data: authData, error: authError } = await adminClient.auth.getUser(bearer);
  if (authError || !authData.user) {
    console.error("delete_user failed", {
      code: "UNAUTHORIZED",
      message: authError?.message ?? "Auth verification failed in delete_user",
    });
    return json(
      {
        success: false,
        code: "UNAUTHORIZED",
        message: "Unauthorized",
      },
      401
    );
  }

  const authenticatedUserId = authData.user.id;
  let requestedUserId = authenticatedUserId;

  try {
    const payload = await req.json();
    if (typeof payload?.user_id === "string" && payload.user_id.trim().length > 0) {
      requestedUserId = payload.user_id.trim();
    }
  } catch {
    // Ignore malformed body and fallback to authenticated user id.
  }

  if (requestedUserId !== authenticatedUserId) {
    return json(
      {
        success: false,
        code: "FORBIDDEN",
        message: "Forbidden",
      },
      403
    );
  }

  const nowIso = new Date().toISOString();

  const { error: interestCleanupError } = await adminClient
    .from("user_interest_profiles")
    .delete()
    .eq("user_id", authenticatedUserId);

  if (interestCleanupError && !isRelationMissing(interestCleanupError)) {
    console.error("delete_user failed", {
      code: "INTEREST_PROFILE_CLEANUP_FAILED",
      dbCode: interestCleanupError.code,
      message: interestCleanupError.message,
      details: interestCleanupError.details,
      hint: interestCleanupError.hint,
    });
    return json(
      {
        success: false,
        code: "INTEREST_PROFILE_CLEANUP_FAILED",
        message: "Failed to cleanup interest profile",
      },
      500
    );
  }

  const { error: subscriptionCleanupError } = await adminClient
    .from("notification_subscriptions")
    .delete()
    .eq("user_id", authenticatedUserId);

  if (subscriptionCleanupError && !isRelationMissing(subscriptionCleanupError)) {
    console.error("delete_user failed", {
      code: "SUBSCRIPTION_CLEANUP_FAILED",
      dbCode: subscriptionCleanupError.code,
      message: subscriptionCleanupError.message,
      details: subscriptionCleanupError.details,
      hint: subscriptionCleanupError.hint,
    });
    return json(
      {
        success: false,
        code: "SUBSCRIPTION_CLEANUP_FAILED",
        message: "Failed to cleanup notification subscriptions",
      },
      500
    );
  }

  const { error: profileError } = await adminClient
    .from("profiles")
    .update({
      is_deleted: true,
      deleted_at: nowIso,
      avatar_url: null,
    })
    .eq("user_id", authenticatedUserId);

  if (profileError) {
    console.error("delete_user failed", {
      code: "PROFILE_UPDATE_FAILED",
      dbCode: profileError.code,
      message: profileError.message,
      details: profileError.details,
      hint: profileError.hint,
    });
    return json(
      {
        success: false,
        code: "PROFILE_UPDATE_FAILED",
        message: "Failed to update profile",
      },
      500
    );
  }

  const { error: authDeleteError } = await adminClient.auth.admin.deleteUser(
    authenticatedUserId,
    true
  );
  if (authDeleteError) {
    console.error("delete_user failed", {
      code: "AUTH_DELETE_FAILED",
      message: authDeleteError.message,
    });
    return json(
      {
        success: false,
        code: "AUTH_DELETE_FAILED",
        message: "Failed to delete auth user",
      },
      500
    );
  }

  return json({ success: true, code: "OK" });
});
