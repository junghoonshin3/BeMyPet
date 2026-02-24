package kr.sjh.core.model

enum class KakaoEmailVerificationReason {
    NO_EMAIL,
    UNVERIFIED_EMAIL,
}

sealed class SessionState {
    data class Authenticated(val user: User) : SessionState()
    data class EmailVerificationRequired(val reason: KakaoEmailVerificationReason) : SessionState()
    data class NoAuthenticated(val isSignOut: Boolean) : SessionState()
    data object RefreshFailure : SessionState()
    data object Initializing : SessionState()
    data class Banned(val bannedUntil: String) : SessionState()
}
