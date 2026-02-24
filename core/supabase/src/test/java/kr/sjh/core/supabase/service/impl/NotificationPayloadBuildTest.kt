package kr.sjh.core.supabase.service.impl

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NotificationPayloadBuildTest {

    @Test
    fun `build upsert payload trims token and normalizes timezone`() {
        val payload = NotificationServiceImpl.buildSubscriptionRpcPayloadForTest(
            fcmToken = " token ",
            pushOptIn = true,
            timezone = "",
        )

        assertEquals("token", payload.fcmToken)
        assertEquals("Asia/Seoul", payload.timezone)
        assertEquals(true, payload.pushOptIn)
    }

    @Test
    fun `subscription payload should be serializable for postgrest`() {
        val payload = NotificationServiceImpl.buildSubscriptionRpcPayloadForTest(
            fcmToken = "token",
            pushOptIn = true,
            timezone = "",
        )

        val encoded = Json.encodeToString(payload)

        assertTrue(encoded.contains("\"push_opt_in\":true"))
    }
}
