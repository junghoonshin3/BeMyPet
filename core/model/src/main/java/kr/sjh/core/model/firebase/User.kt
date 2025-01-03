package kr.sjh.core.model.firebase

import androidx.annotation.Keep
import androidx.compose.runtime.Stable


@Stable
data class User @Keep constructor(
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val profilePictureUrl: String = "",
//    val fcmToken: String = "",
    val provider: String = Provider.DEFAULT.name
)

enum class Provider {
    DEFAULT, GOOGLE
}