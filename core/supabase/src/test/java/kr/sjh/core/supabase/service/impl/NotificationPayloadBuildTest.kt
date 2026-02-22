package kr.sjh.core.supabase.service.impl

import kotlin.test.Test
import kotlin.test.assertEquals

class NotificationPayloadBuildTest {

    @Test
    fun `build upsert payload trims token and normalizes timezone`() {
        val payload = NotificationServiceImpl.buildSubscriptionPayloadForTest(
            userId = "u1",
            fcmToken = " token ",
            timezone = "",
        )

        assertEquals("token", payload["fcm_token"])
        assertEquals("Asia/Seoul", payload["timezone"])
    }
}
