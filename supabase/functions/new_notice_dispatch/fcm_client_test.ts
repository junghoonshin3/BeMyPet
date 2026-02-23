import {
  buildAccessTokenRequest,
  buildSummaryMessage,
} from "./fcm_client.ts";

Deno.test("buildAccessTokenRequest uses service account scope", () => {
  const req = buildAccessTokenRequest(
    JSON.stringify({
      client_email: "firebase-adminsdk@example.iam.gserviceaccount.com",
      private_key: "-----BEGIN PRIVATE KEY-----\\nABC\\n-----END PRIVATE KEY-----\\n",
      token_uri: "https://oauth2.googleapis.com/token",
    }),
  );

  if (!req.scope.includes("https://www.googleapis.com/auth/firebase.messaging")) {
    throw new Error(`scope missing: ${req.scope}`);
  }
});

Deno.test("buildSummaryMessage maps payload fields", () => {
  const msg = buildSummaryMessage("token-1", "3", "batch-1");
  if (msg.message.data.matched_count !== "3") {
    throw new Error(`payload mismatch: ${JSON.stringify(msg)}`);
  }
  if (msg.message.data.batch_id !== "batch-1") {
    throw new Error(`batch mismatch: ${JSON.stringify(msg)}`);
  }
  if (msg.message.data.campaign_type !== "new_animal_summary") {
    throw new Error(`campaign mismatch: ${JSON.stringify(msg)}`);
  }
});
