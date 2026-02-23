package kr.sjh.core.supabase.service.impl

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotificationAuthRetryPolicyTest {

    @Test
    fun `should retry when status code is unauthorized`() {
        assertTrue(
            NotificationServiceImpl.shouldRetryAfterAuthRefreshForTest(
                statusCode = 401,
                message = null,
                errorPayload = null,
            )
        )
    }

    @Test
    fun `should retry when jwt expired message is present`() {
        assertTrue(
            NotificationServiceImpl.shouldRetryAfterAuthRefreshForTest(
                statusCode = null,
                message = "JWT expired",
                errorPayload = null,
            )
        )
    }

    @Test
    fun `should not retry for non auth related errors`() {
        assertFalse(
            NotificationServiceImpl.shouldRetryAfterAuthRefreshForTest(
                statusCode = 500,
                message = "duplicate key value violates unique constraint",
                errorPayload = """{"code":"23505"}""",
            )
        )
    }
}
