package kr.sjh.core.ktor.model.request

import kotlinx.serialization.Serializable
import kr.sjh.core.ktor.BuildConfig
import kr.sjh.core.ktor.model.JSON
import kr.sjh.core.ktor.model.XML

@Serializable
data class SigunguRequest(
    val serviceKey: String = BuildConfig.SERVICE_KEY,
    val upr_cd: String? = null,
    val _type: String = XML
)