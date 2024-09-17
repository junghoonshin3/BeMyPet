package kr.sjh.core.ktor.model.request

import kotlinx.serialization.Serializable
import kr.sjh.core.ktor.model.JSON

@Serializable
data class KindRequest(
    val serviceKey: String,
    val up_kind_cd: String? = null, // 축종 코드 (optional)
    val _type: String = JSON // 기본값 xml
)