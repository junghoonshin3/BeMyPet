package kr.sjh.feature.signup.navigation

import kotlinx.serialization.Serializable

@Serializable
data object SignUp

@Serializable
data object Onboarding

@Serializable
data class KakaoEmailVerificationNotice(
    val reason: String,
)
