package kr.sjh.core.ktor.model.request

import kotlinx.serialization.Serializable
import kr.sjh.core.ktor.model.JSON

@Serializable
data class SigunguRequest(
    val serviceKey: String,
    val upr_cd: String,
    val _type: String = JSON
)