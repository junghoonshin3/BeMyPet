package kr.sjh.feature.report.navigation

import kotlinx.serialization.Serializable
import kr.sjh.core.model.ReportType

@Serializable
data class Report(
    val type: ReportType,
    val reportedUserId: String,
    val reportByUserId: String,
    val commentId: String? = null
)
