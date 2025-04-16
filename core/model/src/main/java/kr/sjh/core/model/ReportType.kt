package kr.sjh.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class ReportType(val title: String) {
    Comment("댓글"), User("사용자")
}