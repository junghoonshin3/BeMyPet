package kr.sjh.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class User(
    @SerialName("id") val id: String,
    @SerialName("raw_user_meta_data") val rawUserMetaData: JsonObject,
    @SerialName("role") val role: Role = Role.USER,
    @SerialName("isBanned") val isBanned: Boolean = false,
    @SerialName("banned_until") val bannedUntil: String?
)

@Serializable
enum class Role(val role: String) {
    USER("user"), ADMIN("admin")
}