package kr.sjh.core.ktor.model.request

import kotlinx.serialization.Serializable
import kr.sjh.core.ktor.model.JSON

@Serializable
data class ShelterRequest(
    val serviceKey: String,
    val upr_cd: String? = null, // 시도 코드 (optional)
    val org_cd: String? = null, // 시군구 코드 (optional)
    val _type: String = JSON // 기본값 xml
)