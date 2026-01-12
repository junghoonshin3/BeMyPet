package kr.sjh.core.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
enum class ReportType(val title: String) {
    Comment("댓글"), User("사용자")
}