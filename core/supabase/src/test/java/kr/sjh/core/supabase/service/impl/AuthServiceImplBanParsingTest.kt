package kr.sjh.core.supabase.service.impl

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthServiceImplBanParsingTest {

    @Test
    fun `isBannedNowForTest returns true for Z timestamp in future`() {
        val now = Instant.parse("2026-02-21T10:00:00Z")
        val bannedUntil = "2026-02-21T10:01:00Z"

        assertTrue(AuthServiceImpl.isBannedNowForTest(bannedUntil, now))
    }

    @Test
    fun `isBannedNowForTest returns true for offset timestamp in future`() {
        val now = Instant.parse("2026-02-21T10:00:00Z")
        val bannedUntil = "2026-02-21T19:01:00+09:00"

        assertTrue(AuthServiceImpl.isBannedNowForTest(bannedUntil, now))
    }

    @Test
    fun `isBannedNowForTest supports local datetime with UTC policy`() {
        val now = Instant.parse("2026-02-21T10:00:00Z")
        val bannedUntil = "2026-02-21T10:01:00"

        assertTrue(AuthServiceImpl.isBannedNowForTest(bannedUntil, now))
    }

    @Test
    fun `isBannedNowForTest returns false for past datetime`() {
        val now = Instant.parse("2026-02-21T10:00:00Z")
        val bannedUntil = "2026-02-21T09:59:59Z"

        assertFalse(AuthServiceImpl.isBannedNowForTest(bannedUntil, now))
    }

    @Test
    fun `parseBannedUntilInstantForTest returns null for invalid value`() {
        val parsed = AuthServiceImpl.parseBannedUntilInstantForTest("not-a-datetime")

        assertNull(parsed)
    }
}
