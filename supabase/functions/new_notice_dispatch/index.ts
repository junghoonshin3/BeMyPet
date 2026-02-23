import { createClient } from "jsr:@supabase/supabase-js@2";
import {
  buildDateWindow,
  buildNoticeKey,
  chunkKeysForInFilter,
} from "./dispatch_core.ts";
import { classifyFcmError, getAccessToken, sendSummaryMessage } from "./fcm_client.ts";

type DispatchNoticeInput = {
  notice_no?: string;
  desertion_no?: string;
  upr_cd?: string;
  org_cd?: string;
  upkind?: string;
  sex_cd?: string;
  weight?: string;
  upd_tm?: string;
};

type DispatchRequest = {
  dry_run?: boolean;
  notices?: DispatchNoticeInput[];
  max_pages?: number;
  num_of_rows?: number;
};

type DispatchStateRow = {
  id: number;
  last_success_date: string | null;
};

type SubscriptionRow = {
  user_id: string;
  fcm_token: string;
  push_opt_in: boolean;
};

type NormalizedNotice = {
  noticeNo: string;
  desertionNo: string;
  noticeKey: string;
  uprCd: string;
  orgCd: string;
  upkind: string;
  sexCd: string;
  sizeCategory: string;
  sourceUpdatedDate: string | null;
};

const DEFAULT_PUBLIC_API_BASE_URL = "https://apis.data.go.kr/1543061/abandonmentPublicService_v2/";
const DEFAULT_MAX_PAGES = 5;
const DEFAULT_NUM_OF_ROWS = 100;
const MAX_PAGE_LIMIT = 20;
const MAX_TOKEN_SEND_PER_RUN = 5000;

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

function clampInt(value: unknown, min: number, max: number, fallback: number): number {
  const parsed = Number(value);
  if (!Number.isFinite(parsed)) return fallback;
  const rounded = Math.floor(parsed);
  return Math.max(min, Math.min(max, rounded));
}

function normalizeText(value: unknown): string {
  return typeof value == "string" ? value.trim() : "";
}

function normalizeBaseUrl(baseUrl: string): string {
  return baseUrl.endsWith("/") ? baseUrl : `${baseUrl}/`;
}

function readString(row: Record<string, unknown>, keys: string[]): string {
  for (const key of keys) {
    const value = row[key];
    const normalized = normalizeText(value);
    if (normalized) return normalized;
  }
  return "";
}

function toApiYmd(dateLike: string): string {
  return dateLike.replace(/-/g, "").slice(0, 8);
}

function normalizeDateOnly(value: string): string | null {
  const raw = value.trim();
  if (!raw) return null;
  const match = raw.match(/^(\d{4})[-.]?(\d{2})[-.]?(\d{2})/);
  if (!match) return null;
  return `${match[1]}-${match[2]}-${match[3]}`;
}

function classifySizeCategory(weightRaw: string): string {
  const match = weightRaw.replace(/,/g, "").match(/\d+(?:\.\d+)?/);
  if (!match) return "";
  const value = Number(match[0]);
  if (!Number.isFinite(value)) return "";
  if (value <= 5) return "SMALL";
  if (value <= 15) return "MEDIUM";
  return "LARGE";
}

async function shortHash(input: string): Promise<string> {
  const digest = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(input));
  return Array.from(new Uint8Array(digest))
    .slice(0, 8)
    .map((n) => n.toString(16).padStart(2, "0"))
    .join("");
}

function extractApiItems(payload: any): Record<string, unknown>[] {
  const item = payload?.response?.body?.items?.item;
  if (Array.isArray(item)) {
    return item.filter((x) => x && typeof x == "object") as Record<string, unknown>[];
  }
  if (item && typeof item == "object") {
    return [item as Record<string, unknown>];
  }
  return [];
}

function normalizeNoticeFromRow(row: Record<string, unknown>): NormalizedNotice | null {
  const noticeNo = readString(row, ["noticeNo", "notice_no"]);
  const desertionNo = readString(row, ["desertionNo", "desertion_no"]);
  const noticeKey = buildNoticeKey({ noticeNo, desertionNo });
  if (!noticeKey) return null;

  const uprCd = readString(row, ["uprCd", "upr_cd"]);
  const orgCd = readString(row, ["orgCd", "org_cd"]);
  const upkind = readString(row, ["upKindCd", "upkind", "up_kind_cd"]);
  const sexCd = readString(row, ["sexCd", "sex_cd"]);
  const weight = readString(row, ["weight"]);
  const updTm = readString(row, ["updTm", "upd_tm"]);

  return {
    noticeNo,
    desertionNo,
    noticeKey,
    uprCd,
    orgCd,
    upkind,
    sexCd,
    sizeCategory: classifySizeCategory(weight),
    sourceUpdatedDate: normalizeDateOnly(updTm),
  };
}

function normalizePayloadNotices(input: DispatchNoticeInput[]): NormalizedNotice[] {
  const normalized: NormalizedNotice[] = [];
  for (const row of input) {
    const mapped = normalizeNoticeFromRow({
      noticeNo: row.notice_no,
      desertionNo: row.desertion_no,
      uprCd: row.upr_cd,
      orgCd: row.org_cd,
      upKindCd: row.upkind,
      sexCd: row.sex_cd,
      weight: row.weight,
      updTm: row.upd_tm,
    });
    if (mapped) {
      normalized.push(mapped);
    }
  }
  return normalized;
}

async function fetchPublicApiNotices(input: {
  baseUrl: string;
  serviceKey: string;
  bgupd: string;
  enupd: string;
  maxPages: number;
  numOfRows: number;
}): Promise<NormalizedNotice[]> {
  const notices: NormalizedNotice[] = [];
  const baseUrl = normalizeBaseUrl(input.baseUrl);

  for (let pageNo = 1; pageNo <= input.maxPages; pageNo += 1) {
    const url = new URL("abandonmentPublic_v2", baseUrl);
    url.searchParams.set("serviceKey", input.serviceKey);
    url.searchParams.set("_type", "json");
    url.searchParams.set("bgupd", input.bgupd);
    url.searchParams.set("enupd", input.enupd);
    url.searchParams.set("pageNo", String(pageNo));
    url.searchParams.set("numOfRows", String(input.numOfRows));

    const response = await fetch(url);
    const payload = await response.json().catch(() => ({}));
    if (!response.ok) {
      throw new Error(`Public API request failed: status=${response.status}`);
    }

    const resultCode = normalizeText(payload?.response?.header?.resultCode);
    if (resultCode && resultCode != "00") {
      const resultMsg = normalizeText(payload?.response?.header?.resultMsg);
      throw new Error(`Public API error: code=${resultCode} msg=${resultMsg}`);
    }

    const items = extractApiItems(payload)
      .map(normalizeNoticeFromRow)
      .filter((row): row is NormalizedNotice => row != null);

    notices.push(...items);

    const totalCount = Number(payload?.response?.body?.totalCount ?? 0);
    if (items.length == 0) break;
    if (Number.isFinite(totalCount) && totalCount > 0 && notices.length >= totalCount) {
      break;
    }
  }

  return notices;
}

async function loadDispatchState(adminClient: any): Promise<DispatchStateRow | null> {
  const { data, error } = await adminClient
    .from("notification_dispatch_state")
    .select("id,last_success_date")
    .eq("id", 1)
    .maybeSingle();

  if (error) {
    throw new Error(`Failed to load dispatch state: ${error.message}`);
  }
  return data as DispatchStateRow | null;
}

async function markDispatchStarted(adminClient: any, runStartedAt: string): Promise<void> {
  const { error } = await adminClient
    .from("notification_dispatch_state")
    .upsert(
      {
        id: 1,
        last_run_started_at: runStartedAt,
      },
      { onConflict: "id" },
    );
  if (error) {
    throw new Error(`Failed to mark dispatch start: ${error.message}`);
  }
}

async function markDispatchCompleted(adminClient: any, input: {
  runCompletedAt: string;
  successDate: string;
  errorMessage?: string;
}): Promise<void> {
  const payload: Record<string, unknown> = {
    id: 1,
    last_run_completed_at: input.runCompletedAt,
  };

  if (input.errorMessage) {
    payload.last_error_at = input.runCompletedAt;
    payload.last_error_message = input.errorMessage.slice(0, 500);
  } else {
    payload.last_success_date = input.successDate;
    payload.last_error_at = null;
    payload.last_error_message = null;
  }

  await adminClient
    .from("notification_dispatch_state")
    .upsert(payload, { onConflict: "id" });
}

async function cleanupExpiredSeenNotices(adminClient: any, nowIso: string): Promise<void> {
  await adminClient
    .from("notification_seen_notices")
    .delete()
    .lte("expires_at", nowIso);
}

async function loadExistingNoticeKeys(adminClient: any, noticeKeys: string[]): Promise<Set<string>> {
  const existing = new Set<string>();
  for (const keys of chunkKeysForInFilter(noticeKeys)) {
    const { data, error } = await adminClient
      .from("notification_seen_notices")
      .select("notice_key")
      .in("notice_key", keys);

    if (error) {
      throw new Error(`Failed to read seen notices: ${error.message}`);
    }

    for (const row of data ?? []) {
      const key = normalizeText((row as any).notice_key);
      if (key) existing.add(key);
    }
  }
  return existing;
}

async function insertSeenNotices(adminClient: any, notices: NormalizedNotice[]): Promise<void> {
  if (notices.length == 0) return;

  const rows = notices.map((notice) => ({
    notice_key: notice.noticeKey,
    notice_no: notice.noticeNo || null,
    desertion_no: notice.desertionNo || null,
    source_updated_date: notice.sourceUpdatedDate,
    expires_at: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString(),
  }));

  const { error } = await adminClient
    .from("notification_seen_notices")
    .upsert(rows, { onConflict: "notice_key", ignoreDuplicates: true });

  if (error) {
    throw new Error(`Failed to insert seen notices: ${error.message}`);
  }
}

async function loadSubscriptions(adminClient: any): Promise<SubscriptionRow[]> {
  const { data, error } = await adminClient
    .from("notification_subscriptions")
    .select("user_id,fcm_token,push_opt_in")
    .eq("push_opt_in", true);

  if (error) {
    throw new Error(`Failed to query subscriptions: ${error.message}`);
  }
  return (data ?? []) as SubscriptionRow[];
}

function buildSubscriptionTokenMap(subscriptions: SubscriptionRow[]): Map<string, string[]> {
  const tokenMap = new Map<string, string[]>();

  for (const row of subscriptions) {
    const userId = normalizeText(row.user_id);
    const token = normalizeText(row.fcm_token);
    if (!userId || !token) continue;

    const current = tokenMap.get(userId) ?? [];
    if (!current.includes(token)) {
      current.push(token);
      tokenMap.set(userId, current);
    }
  }

  return tokenMap;
}

function buildBroadcastUserSummaries(
  tokenMap: Map<string, string[]>,
  notices: NormalizedNotice[],
): Array<{ userId: string; noticeKeys: string[]; matchedCount: number }> {
  if (notices.length == 0) return [];

  const noticeKeys = notices.map((notice) => notice.noticeKey);
  return Array.from(tokenMap.entries())
    .filter(([userId, tokens]) => userId.length > 0 && tokens.length > 0)
    .map(([userId]) => ({
      userId,
      noticeKeys,
      matchedCount: noticeKeys.length,
    }))
    .sort((a, b) => a.userId.localeCompare(b.userId));
}

async function upsertDeliveryLog(adminClient: any, input: {
  userId: string;
  dedupeKey: string;
  noticeNo: string | null;
  status: "sent" | "failed";
  payloadJson: Record<string, unknown>;
  sentAt: string;
}) {
  await adminClient
    .from("notification_delivery_logs")
    .upsert(
      {
        user_id: input.userId,
        campaign_type: "new_animal",
        notice_no: input.noticeNo,
        dedupe_key: input.dedupeKey,
        status: input.status,
        payload_json: input.payloadJson,
        sent_at: input.sentAt,
      },
      { onConflict: "dedupe_key" },
    );
}

async function deleteInvalidTokens(adminClient: any, tokens: string[]): Promise<number> {
  const uniqueTokens = Array.from(new Set(tokens.map((token) => token.trim()).filter((token) => token.length > 0)));
  if (uniqueTokens.length == 0) return 0;

  const { data, error } = await adminClient
    .from("notification_subscriptions")
    .delete()
    .in("fcm_token", uniqueTokens)
    .select("fcm_token");

  if (error) {
    throw new Error(`Failed to delete invalid tokens: ${error.message}`);
  }
  return (data ?? []).length;
}

Deno.serve(async (req) => {
  const runStartedAt = new Date().toISOString();
  let adminClient: any = null;

  try {
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

    adminClient = createClient(supabaseUrl, serviceRoleKey, {
      auth: {
        autoRefreshToken: false,
        persistSession: false,
      },
    });

    const isServiceInvoker = bearer == serviceRoleKey || parseJwtRole(bearer) == "service_role";
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
    await markDispatchStarted(adminClient, runStartedAt);

    const state = await loadDispatchState(adminClient);
    const todayIso = runStartedAt.slice(0, 10);
    const runWindow = buildDateWindow(state?.last_success_date ?? null, todayIso);

    await cleanupExpiredSeenNotices(adminClient, runStartedAt);

    const normalizedPayloadNotices = normalizePayloadNotices(Array.isArray(payload.notices) ? payload.notices : []);
    let fetchedNotices: NormalizedNotice[] = normalizedPayloadNotices;

    if (fetchedNotices.length == 0) {
      const publicApiServiceKey = normalizeText(Deno.env.get("PUBLIC_PET_API_SERVICE_KEY") ?? "");
      if (!publicApiServiceKey) {
        throw new Error("Missing env PUBLIC_PET_API_SERVICE_KEY");
      }

      fetchedNotices = await fetchPublicApiNotices({
        baseUrl: normalizeText(Deno.env.get("PUBLIC_PET_API_BASE_URL") ?? DEFAULT_PUBLIC_API_BASE_URL),
        serviceKey: publicApiServiceKey,
        bgupd: toApiYmd(runWindow.bgupd),
        enupd: toApiYmd(runWindow.enupd),
        maxPages: clampInt(payload.max_pages, 1, MAX_PAGE_LIMIT, DEFAULT_MAX_PAGES),
        numOfRows: clampInt(payload.num_of_rows, 1, 1000, DEFAULT_NUM_OF_ROWS),
      });
    }

    const uniqueNoticeMap = new Map<string, NormalizedNotice>();
    for (const notice of fetchedNotices) {
      if (!uniqueNoticeMap.has(notice.noticeKey)) {
        uniqueNoticeMap.set(notice.noticeKey, notice);
      }
    }
    const uniqueNotices = Array.from(uniqueNoticeMap.values());

    const existingNoticeKeys = await loadExistingNoticeKeys(
      adminClient,
      uniqueNotices.map((notice) => notice.noticeKey),
    );

    const newNotices = uniqueNotices.filter((notice) => !existingNoticeKeys.has(notice.noticeKey));

    if (!dryRun) {
      await insertSeenNotices(adminClient, newNotices);
    }

    const subscriptions = await loadSubscriptions(adminClient);
    const tokenMap = buildSubscriptionTokenMap(subscriptions);
    const userSummaries = buildBroadcastUserSummaries(tokenMap, newNotices);
    const matchedUsers = userSummaries.length;
    const targetTokenCount = userSummaries.reduce(
      (acc, summary) => acc + (tokenMap.get(summary.userId)?.length ?? 0),
      0,
    );

    let sentCount = 0;
    let failedCount = 0;
    let invalidTokenDeletedCount = 0;
    const invalidTokens: string[] = [];

    if (!dryRun && targetTokenCount > 0) {
      if (targetTokenCount > MAX_TOKEN_SEND_PER_RUN) {
        throw new Error(`Token send limit exceeded: ${targetTokenCount}`);
      }

      const firebaseProjectId = normalizeText(Deno.env.get("FIREBASE_PROJECT_ID") ?? "");
      const firebaseServiceAccountJson = normalizeText(Deno.env.get("FIREBASE_SERVICE_ACCOUNT_JSON") ?? "");
      if (!firebaseProjectId || !firebaseServiceAccountJson) {
        throw new Error("Missing firebase configuration");
      }

      const accessToken = await getAccessToken(firebaseServiceAccountJson);
      const batchId = await shortHash(`${runWindow.bgupd}:${runWindow.enupd}:${newNotices.map((n) => n.noticeKey).sort().join("|")}`);

      const noticeNoFallback = newNotices[0]?.noticeNo || null;

      for (const summary of userSummaries) {
        const tokens = tokenMap.get(summary.userId) ?? [];
        for (const token of tokens) {
          const tokenKey = await shortHash(token);
          const dedupeKey = `new_animal_summary:${summary.userId}:${batchId}:${tokenKey}`;
          const result = await sendSummaryMessage({
            projectId: firebaseProjectId,
            accessToken,
            token,
            matchedCount: String(summary.matchedCount),
            batchId,
          });

          const status = result.ok ? "sent" : "failed";
          if (result.ok) {
            sentCount += 1;
          } else {
            failedCount += 1;
            const errorKind = classifyFcmError(result.response);
            if (errorKind == "invalid_token") {
              invalidTokens.push(token);
            }
          }

          await upsertDeliveryLog(adminClient, {
            userId: summary.userId,
            dedupeKey,
            noticeNo: noticeNoFallback,
            status,
            payloadJson: {
              batch_id: batchId,
              campaign_type: "new_animal_summary",
              matched_count: summary.matchedCount,
              notice_keys: summary.noticeKeys,
              token_hash: tokenKey,
              response: result.response,
            },
            sentAt: new Date().toISOString(),
          });
        }
      }

      invalidTokenDeletedCount = await deleteInvalidTokens(adminClient, invalidTokens);
    }

    await markDispatchCompleted(adminClient, {
      runCompletedAt: new Date().toISOString(),
      successDate: runWindow.enupd,
    });

    return json({
      dry_run: dryRun,
      window: runWindow,
      fetched_notice_count: uniqueNotices.length,
      new_notice_count: newNotices.length,
      matched_users: matchedUsers,
      target_token_count: targetTokenCount,
      sent_count: sentCount,
      failed_count: failedCount,
      invalid_token_deleted_count: invalidTokenDeletedCount,
    });
  } catch (error) {
    if (adminClient) {
      await markDispatchCompleted(adminClient, {
        runCompletedAt: new Date().toISOString(),
        successDate: runStartedAt.slice(0, 10),
        errorMessage: String((error as Error)?.message ?? error),
      });
    }

    return json(
      {
        error: "Dispatch failed",
        details: String((error as Error)?.message ?? error),
      },
      500,
    );
  }
});
