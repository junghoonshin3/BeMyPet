const FIREBASE_MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
const OAUTH_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer";
const DEFAULT_TOKEN_URI = "https://oauth2.googleapis.com/token";

type ServiceAccount = {
  client_email: string;
  private_key: string;
  token_uri?: string;
};

export type AccessTokenRequest = {
  tokenUri: string;
  grantType: string;
  scope: string;
  clientEmail: string;
  privateKey: string;
};

export type SummaryMessage = {
  message: {
    token: string;
    notification: {
      title: string;
      body: string;
    };
    data: Record<string, string>;
  };
};

export type SendSummaryInput = {
  projectId: string;
  accessToken: string;
  token: string;
  matchedCount: string;
  batchId: string;
};

export type SendResult = {
  ok: boolean;
  status: number;
  response: unknown;
};

function base64UrlEncode(input: string | Uint8Array): string {
  const bytes = typeof input == "string" ? new TextEncoder().encode(input) : input;
  let binary = "";
  for (const byte of bytes) {
    binary += String.fromCharCode(byte);
  }
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/g, "");
}

function pemToDer(privateKeyPem: string): ArrayBuffer {
  const normalized = privateKeyPem.replace(/\\n/g, "\n");
  const body = normalized
    .replace("-----BEGIN PRIVATE KEY-----", "")
    .replace("-----END PRIVATE KEY-----", "")
    .replace(/\s+/g, "");
  const binary = atob(body);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) {
    bytes[i] = binary.charCodeAt(i);
  }
  return bytes.buffer.slice(bytes.byteOffset, bytes.byteOffset + bytes.byteLength);
}

function parseServiceAccount(serviceAccountJson: string): ServiceAccount {
  const parsed = JSON.parse(serviceAccountJson) as ServiceAccount;
  const clientEmail = parsed.client_email?.trim();
  const privateKey = parsed.private_key?.trim();
  if (!clientEmail || !privateKey) {
    throw new Error("Invalid FIREBASE_SERVICE_ACCOUNT_JSON");
  }
  return {
    client_email: clientEmail,
    private_key: privateKey,
    token_uri: parsed.token_uri?.trim() || DEFAULT_TOKEN_URI,
  };
}

async function buildJwtAssertion(request: AccessTokenRequest): Promise<string> {
  const now = Math.floor(Date.now() / 1000);
  const header = { alg: "RS256", typ: "JWT" };
  const claims = {
    iss: request.clientEmail,
    scope: request.scope,
    aud: request.tokenUri,
    iat: now,
    exp: now + 3600,
  };

  const encodedHeader = base64UrlEncode(JSON.stringify(header));
  const encodedClaims = base64UrlEncode(JSON.stringify(claims));
  const signingInput = `${encodedHeader}.${encodedClaims}`;

  const key = await crypto.subtle.importKey(
    "pkcs8",
    pemToDer(request.privateKey),
    {
      name: "RSASSA-PKCS1-v1_5",
      hash: "SHA-256",
    },
    false,
    ["sign"],
  );
  const signature = await crypto.subtle.sign(
    { name: "RSASSA-PKCS1-v1_5" },
    key,
    new TextEncoder().encode(signingInput),
  );
  return `${signingInput}.${base64UrlEncode(new Uint8Array(signature))}`;
}

export function buildAccessTokenRequest(serviceAccountJson: string): AccessTokenRequest {
  const serviceAccount = parseServiceAccount(serviceAccountJson);
  return {
    tokenUri: serviceAccount.token_uri || DEFAULT_TOKEN_URI,
    grantType: OAUTH_GRANT_TYPE,
    scope: FIREBASE_MESSAGING_SCOPE,
    clientEmail: serviceAccount.client_email,
    privateKey: serviceAccount.private_key,
  };
}

export async function getAccessToken(serviceAccountJson: string): Promise<string> {
  const request = buildAccessTokenRequest(serviceAccountJson);
  const assertion = await buildJwtAssertion(request);
  const body = new URLSearchParams({
    grant_type: request.grantType,
    assertion,
  });

  const response = await fetch(request.tokenUri, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body,
  });
  const payload = await response.json().catch(() => ({}));
  if (!response.ok || typeof payload?.access_token != "string") {
    throw new Error(`Failed to get access token: status=${response.status}`);
  }
  return payload.access_token;
}

export function buildSummaryMessage(token: string, matchedCount: string, batchId: string): SummaryMessage {
  return {
    message: {
      token,
      notification: {
        title: "새 공고 알림",
        body: `관심 조건에 맞는 신규 공고 ${matchedCount}건이 등록됐어요.`,
      },
      data: {
        campaign_type: "new_animal_summary",
        matched_count: matchedCount,
        batch_id: batchId,
      },
    },
  };
}

export async function sendSummaryMessage(input: SendSummaryInput): Promise<SendResult> {
  const url = `https://fcm.googleapis.com/v1/projects/${encodeURIComponent(input.projectId)}/messages:send`;
  const payload = buildSummaryMessage(input.token, input.matchedCount, input.batchId);
  const response = await fetch(url, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${input.accessToken}`,
      "Content-Type": "application/json; charset=UTF-8",
    },
    body: JSON.stringify(payload),
  });
  const body = await response.json().catch(() => ({}));
  return {
    ok: response.ok,
    status: response.status,
    response: body,
  };
}

export function classifyFcmError(errorBody: unknown): "invalid_token" | "retryable" | "fatal" {
  const raw = JSON.stringify(errorBody ?? {}).toLowerCase();
  if (
    raw.includes("unregistered") ||
    (raw.includes("invalid_argument") && raw.includes("token"))
  ) {
    return "invalid_token";
  }
  if (raw.includes("unavailable") || raw.includes("internal")) {
    return "retryable";
  }
  return "fatal";
}
