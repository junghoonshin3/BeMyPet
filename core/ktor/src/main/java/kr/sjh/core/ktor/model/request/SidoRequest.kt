package kr.sjh.core.ktor.model.request

import kotlinx.serialization.Serializable
import kr.sjh.core.ktor.BuildConfig
import kr.sjh.core.ktor.model.JSON
import kr.sjh.core.ktor.model.XML

@Serializable
data class SidoRequest(
    val serviceKey: String = BuildConfig.SERVER_KEY,
    val _type: String = XML,
)
