import {
  buildDateWindow,
  buildNoticeKey,
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
