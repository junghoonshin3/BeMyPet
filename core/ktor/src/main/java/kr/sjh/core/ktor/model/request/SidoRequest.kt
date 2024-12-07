package kr.sjh.core.ktor.model.request

import kotlinx.serialization.Serializable
import kr.sjh.core.ktor.BuildConfig
import kr.sjh.core.ktor.model.JSON
import kr.sjh.core.ktor.model.XML

@Serializable
data class SidoRequest(
    val serviceKey: String = BuildConfig.SERVICE_KEY,
    val _type: String = XML,
    val numOfRows: String = "30"
)
