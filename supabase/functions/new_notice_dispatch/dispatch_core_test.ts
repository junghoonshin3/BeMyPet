import {
  buildDateWindow,
  buildNoticeKey,
  chunkKeysForInFilter,
  matchesInterest,
  summarizeByUser,
} from "./dispatch_core.ts";

Deno.test("buildDateWindow applies one-day overlap", () => {
  const window = buildDateWindow("2026-02-22", "2026-02-23");
  if (window.bgupd != "20260221" || window.enupd != "20260223") {
    throw new Error(`window mismatch: ${JSON.stringify(window)}`);
  }
});

Deno.test("buildNoticeKey prefers noticeNo over desertionNo", () => {
  const key = buildNoticeKey({ noticeNo: "N-1", desertionNo: "D-1" });
  if (key !== "N-1") {
    throw new Error(`key mismatch: ${key}`);
  }
});

Deno.test("matchInterest returns true when profile field is empty", () => {
  const ok = matchesInterest(
    { regions: [], species: [], sexes: [], sizes: [] },
    { uprCd: "6110000", upkind: "417000", sexCd: "M", sizeCategory: "SMALL" },
  );
  if (!ok) {
    throw new Error("should match wildcard profile");
  }
});

Deno.test("summarizeByUser groups many notices into one payload", () => {
  const rows = summarizeByUser([
    { userId: "u1", noticeKey: "n1" },
    { userId: "u1", noticeKey: "n2" },
  ]);
  if (rows[0]?.matchedCount !== 2) {
    throw new Error(`grouping failed: ${JSON.stringify(rows)}`);
  }
});

Deno.test("chunkKeysForInFilter splits long encoded keys into smaller in-filter chunks", () => {
  const keys = Array.from({ length: 80 }, (_, i) => `전남-함평-2026-${String(i).padStart(5, "0")}`);
  const chunks = chunkKeysForInFilter(keys, 300);
  if (chunks.length < 2) {
    throw new Error(`expected chunks to split: ${JSON.stringify(chunks)}`);
  }

  for (const chunk of chunks) {
    const encodedLength = chunk.reduce((sum, key) => sum + encodeURIComponent(key).length + 1, 0);
    if (encodedLength > 300) {
      throw new Error(`chunk exceeds max encoded length: ${encodedLength}`);
    }
  }
});
