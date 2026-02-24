package kr.sjh.data.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.scan
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
        .retryWhen { cause, _ ->
            if (cause is CancellationException) {
                return@retryWhen false
            }
            delay(SESSION_RETRY_DELAY_MS)
            true
        }
        .catch { emit(SessionState.NoAuthenticated(isSignOut = false)) }
        .scan<SessionState, SessionState>(SessionState.Initializing) { previous, current ->
            stabilizeSessionTransitionForTest(previous, current)
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = SessionState.Initializing
        )

    internal companion object {
        private const val SESSION_RETRY_DELAY_MS = 300L

        fun stabilizeSessionTransitionForTest(
            previous: SessionState,
            current: SessionState,
        ): SessionState {
            return if (
                current is SessionState.Initializing &&
                previous !is SessionState.Initializing
            ) {
                previous
            } else {
                current
            }
        }
    }
}
