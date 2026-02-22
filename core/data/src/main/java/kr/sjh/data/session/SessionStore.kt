package kr.sjh.data.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kr.sjh.core.model.SessionState
import kr.sjh.data.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionStore @Inject constructor(
    authRepository: AuthRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val session: StateFlow<SessionState> = authRepository.getSessionFlow()
        .catch { emit(SessionState.NoAuthenticated(isSignOut = false)) }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = SessionState.Initializing
        )
}
