package kr.sjh.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReportForm(
    @SerialName("id") val id: String? = null,
    @SerialName("type") val type: ReportType,
    @SerialName("reported_by") val reportedByUser: String,
    @SerialName("reported_user") val reportedUser: String,
    @SerialName("comment_id") val commentId: String? = null,
    @SerialName("reason") val reason: String,
    @SerialName("description") val description: String = "",
)