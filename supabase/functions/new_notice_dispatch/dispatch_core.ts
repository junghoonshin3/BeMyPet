export type DateWindow = {
  bgupd: string;
  enupd: string;
};

export type InterestProfile = {
  regions: string[];
  species: string[];
  sexes: string[];
  sizes: string[];
};

export type NoticeCandidate = {
  uprCd?: string;
  orgCd?: string;
  upkind?: string;
  kindCd?: string;
  sexCd?: string;
  sizeCategory?: string;
};

export type UserNoticeMatch = {
  userId: string;
  noticeKey: string;
};

export type UserNoticeSummary = {
  userId: string;
  noticeKeys: string[];
  matchedCount: number;
};

function parseIsoDate(value: string): Date | null {
  const raw = value.trim();
  if (!raw) return null;

  const normalized = raw.length == 10 ? `${raw}T00:00:00Z` : raw;
  const date = new Date(normalized);
  if (Number.isNaN(date.getTime())) return null;
  return date;
}

function shiftDays(base: Date, amount: number): Date {
  const next = new Date(base.getTime());
  next.setUTCDate(next.getUTCDate() + amount);
  return next;
}

function toYmd(date: Date): string {
  const y = date.getUTCFullYear();
  const m = String(date.getUTCMonth() + 1).padStart(2, "0");
  const d = String(date.getUTCDate()).padStart(2, "0");
  return `${y}${m}${d}`;
}

function normalizeToken(value: string | undefined): string {
  return value?.trim().toLowerCase() ?? "";
}

function normalizeSpeciesFromUpkind(value: string | undefined): string {
  const raw = normalizeToken(value);
  if (!raw) return "";
  if (raw == "417000") return "dog";
  if (raw == "422400") return "cat";
  return raw;
}

function matchesFilter(filter: string[], candidate: string): boolean {
  if (filter.length == 0) return true;
  return filter.map((item) => normalizeToken(item)).includes(normalizeToken(candidate));
}

export function buildDateWindow(lastSuccessDate: string | null, todayIso: string): DateWindow {
  const today = parseIsoDate(todayIso) ?? new Date();
  const anchor = lastSuccessDate ? parseIsoDate(lastSuccessDate) ?? today : today;
  const begin = shiftDays(anchor, -1);
  return {
    bgupd: toYmd(begin),
    enupd: toYmd(today),
  };
}

export function buildNoticeKey(notice: { noticeNo?: string; desertionNo?: string }): string {
  const noticeNo = notice.noticeNo?.trim();
  if (noticeNo) return noticeNo;

  const desertionNo = notice.desertionNo?.trim();
  if (desertionNo) return desertionNo;
  return "";
}

export function matchesInterest(profile: InterestProfile, notice: NoticeCandidate): boolean {
  const regionCandidate = notice.uprCd?.trim() || notice.orgCd?.trim() || "";
  const speciesCandidate = normalizeSpeciesFromUpkind(notice.upkind) || normalizeToken(notice.kindCd);
  const sexCandidate = normalizeToken(notice.sexCd);
  const sizeCandidate = normalizeToken(notice.sizeCategory);

  return (
    matchesFilter(profile.regions, regionCandidate) &&
    matchesFilter(profile.species, speciesCandidate) &&
    matchesFilter(profile.sexes, sexCandidate) &&
    matchesFilter(profile.sizes, sizeCandidate)
  );
}

export function summarizeByUser(rows: UserNoticeMatch[]): UserNoticeSummary[] {
  const grouped = new Map<string, Set<string>>();
  for (const row of rows) {
    const userId = row.userId.trim();
    const noticeKey = row.noticeKey.trim();
    if (!userId || !noticeKey) continue;
    if (!grouped.has(userId)) {
      grouped.set(userId, new Set<string>());
    }
    grouped.get(userId)?.add(noticeKey);
  }

  return Array.from(grouped.entries())
    .map(([userId, keySet]) => {
      const noticeKeys = Array.from(keySet).sort();
      return {
        userId,
        noticeKeys,
        matchedCount: noticeKeys.length,
      };
    })
    .sort((a, b) => a.userId.localeCompare(b.userId));
}
