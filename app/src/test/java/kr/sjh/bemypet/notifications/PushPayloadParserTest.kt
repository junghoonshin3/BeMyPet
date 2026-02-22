package kr.sjh.bemypet.notifications

import org.junit.Assert.assertEquals
import org.junit.Test

class PushPayloadParserTest {

    @Test
    fun `parser extracts notice number from data payload`() {
        val payload = mapOf(
            "notice_no" to "12345",
            "campaign_type" to "new_animal",
        )

        val parsed = PushPayloadParser.parse(payload)

        assertEquals("12345", parsed.noticeNo)
        assertEquals("new_animal", parsed.campaignType)
    }
}
