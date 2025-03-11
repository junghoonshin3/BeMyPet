package kr.sjh.core.supabase.model

import kotlinx.serialization.Serializable

@Serializable
data class UserModel(
    val id: String,
    val email: String,
    val nickname: String,
    val profile_image: String,
    val auth_id: String,
    val kakao_id: String,
    val create_at: String
)