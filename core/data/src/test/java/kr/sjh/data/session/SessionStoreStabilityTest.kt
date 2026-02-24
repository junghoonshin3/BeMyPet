package kr.sjh.data.session

import kr.sjh.core.model.SessionState
import kr.sjh.core.model.User
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionStoreStabilityTest {

    @Test
    fun stabilizeSessionTransition_keeps_previous_authenticated_when_initializing_reemitted() {
        val previous = SessionState.Authenticated(
            user = User(
                id = "user-1",
                bannedUntil = null,
            )
        )

        val result = SessionStore.stabilizeSessionTransitionForTest(
            previous = previous,
            current = SessionState.Initializing,
        )

        assertEquals(previous, result)
    }

    @Test
    fun stabilizeSessionTransition_keeps_initializing_on_first_initializing() {
        val result = SessionStore.stabilizeSessionTransitionForTest(
            previous = SessionState.Initializing,
            current = SessionState.Initializing,
        )

        assertEquals(SessionState.Initializing, result)
    }

    @Test
    fun stabilizeSessionTransition_allows_authenticated_to_not_authenticated_transition() {
        val previous = SessionState.Authenticated(
            user = User(
                id = "user-1",
                bannedUntil = null,
            )
        )
        val current = SessionState.NoAuthenticated(isSignOut = false)

        val result = SessionStore.stabilizeSessionTransitionForTest(
            previous = previous,
            current = current,
        )

        assertEquals(current, result)
    }
}
