package kr.sjh.core.model.notification

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserInterestProfile(
    @SerialName("user_id") val userId: String,
    @SerialName("regions") val regions: List<String> = emptyList(),
    @SerialName("species") val species: List<String> = emptyList(),
    @SerialName("sexes") val sexes: List<String> = emptyList(),
    @SerialName("sizes") val sizes: List<String> = emptyList(),
    @SerialName("push_enabled") val pushEnabled: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)
