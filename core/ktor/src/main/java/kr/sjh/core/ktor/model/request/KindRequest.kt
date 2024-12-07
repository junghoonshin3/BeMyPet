package kr.sjh.core.ktor.model.request

import kotlinx.serialization.Serializable
import kr.sjh.core.ktor.BuildConfig
import kr.sjh.core.ktor.model.JSON
import kr.sjh.core.ktor.model.XML

@Serializable
data class KindRequest(
    val serviceKey: String = BuildConfig.SERVICE_KEY,
    val up_kind_cd: String? = null, // 축종 코드 (optional)
    val _type: String = XML // 기본값 xml
)