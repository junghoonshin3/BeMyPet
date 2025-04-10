package kr.sjh.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class User(
    @SerialName("id")
    val id: String,
    @SerialName("raw_user_meta_data")
    val rawUserMetaData: JsonObject
)