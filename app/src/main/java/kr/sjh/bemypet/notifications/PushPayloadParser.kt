package kr.sjh.bemypet.notifications

data class ParsedPushPayload(
    val noticeNo: String,
    val campaignType: String,
)

object PushPayloadParser {
    fun parse(data: Map<String, String>): ParsedPushPayload = ParsedPushPayload(
        noticeNo = data["notice_no"].orEmpty(),
        campaignType = data["campaign_type"].orEmpty(),
    )
}
