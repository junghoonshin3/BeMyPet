package kr.sjh.core.ktor.model.request

import kotlinx.serialization.Serializable
import kr.sjh.core.ktor.BuildConfig
import kr.sjh.core.ktor.model.JSON

@Serializable
data class SidoRequest(
    val serviceKey: String = BuildConfig.SERVER_KEY,
    val numOfRows: Int = 20,
    val pageNo: Int = 1,
    val _type: String = JSON,
)
