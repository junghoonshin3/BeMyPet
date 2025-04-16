package kr.sjh.core.model


sealed class SessionState {
    data class Authenticated(val user: User) : SessionState()
    data class NoAuthenticated(val isSignOut: Boolean) : SessionState()
    data object RefreshFailure : SessionState()
    data object Initializing : SessionState()
    data class Banned(val bannedUntil: String) : SessionState()
}