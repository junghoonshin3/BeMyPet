package kr.sjh.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import java.util.UUID

@Serializable
data class Comment(
    @SerialName("id") val id: String = UUID.randomUUID().toString(),
    @SerialName("user_id") val userId: String = "",
    @SerialName("post_id") val postId: String? = null,
    @SerialName("content") val content: String = "",
    @SerialName("raw_user_meta_data") val rawUserMetaData: JsonObject? = null,
    @SerialName("created_at") val createdAt: String? = null
)