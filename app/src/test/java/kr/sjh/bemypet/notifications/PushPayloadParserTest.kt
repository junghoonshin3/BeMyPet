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
        assertEquals(null, parsed.matchedCount)
        assertEquals("", parsed.batchId)
    }

    @Test
    fun `parser reads summary payload fields`() {
        val payload = mapOf(
            "campaign_type" to "new_animal_summary",
            "matched_count" to "4",
            "batch_id" to "batch-1",
        )

        val parsed = PushPayloadParser.parse(payload)

        assertEquals("new_animal_summary", parsed.campaignType)
        assertEquals(4, parsed.matchedCount)
        assertEquals("batch-1", parsed.batchId)
    }
}
