package kr.sjh.bemypet.notifications

data class ParsedPushPayload(
    val noticeNo: String,
    val campaignType: String,
    val matchedCount: Int?,
    val batchId: String,
)

object PushPayloadParser {
    fun parse(data: Map<String, String>): ParsedPushPayload = ParsedPushPayload(
        noticeNo = data["notice_no"].orEmpty(),
        campaignType = data["campaign_type"].orEmpty(),
        matchedCount = data["matched_count"]?.trim()?.toIntOrNull(),
        batchId = data["batch_id"].orEmpty(),
    )
}
