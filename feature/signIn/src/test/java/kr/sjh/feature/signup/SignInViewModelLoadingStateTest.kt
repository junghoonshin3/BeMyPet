package kr.sjh.feature.signup

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kr.sjh.core.model.SessionState
import kr.sjh.core.model.UserProfile
import kr.sjh.data.repository.AuthRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SignInViewModelLoadingStateTest {

    @Test
    fun onGoogleSignIn_setsGoogleLoading_thenClearsAfterSuccess() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val fakeRepository = PendingAuthRepository()
            val viewModel = SignInViewModel(fakeRepository)

            viewModel.onGoogleSignIn(idToken = "id-token", nonce = "nonce")
            advanceUntilIdle()

            assertEquals(LoadingProvider.Google, viewModel.uiState.value.loadingProvider)
            assertTrue(viewModel.uiState.value.isLoading)

            fakeRepository.completeGoogleSuccess()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.loadingProvider)
            assertFalse(viewModel.uiState.value.isLoading)
            assertTrue(viewModel.uiState.value.isSignedIn)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun onKakaoSignIn_setsKakaoLoading_thenClearsAfterFailure() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val fakeRepository = PendingAuthRepository()
            val viewModel = SignInViewModel(fakeRepository)

            viewModel.onKakaoSignIn()
            advanceUntilIdle()

            assertEquals(LoadingProvider.Kakao, viewModel.uiState.value.loadingProvider)
            assertTrue(viewModel.uiState.value.isLoading)

            fakeRepository.completeKakaoFailure(IllegalStateException("kakao failed"))
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.loadingProvider)
            assertFalse(viewModel.uiState.value.isLoading)
            assertFalse(viewModel.uiState.value.isSignedIn)
            assertTrue(viewModel.uiState.value.errorMessage?.isNotBlank() == true)
        } finally {
            Dispatchers.resetMain()
        }
    }
}

private class PendingAuthRepository : AuthRepository {
    private var googleSuccess: (() -> Unit)? = null
    private var googleFailure: ((Exception) -> Unit)? = null
    private var kakaoSuccess: (() -> Unit)? = null
    private var kakaoFailure: ((Exception) -> Unit)? = null

    override suspend fun signInWithGoogle(
        idToken: String,
        rawNonce: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        googleSuccess = onSuccess
        googleFailure = onFailure
    }

    override suspend fun signInWithKakao(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        kakaoSuccess = onSuccess
        kakaoFailure = onFailure
    }

    override suspend fun signOut() = Unit

    override fun getSessionFlow(): Flow<SessionState> = emptyFlow()

    override suspend fun deleteAccount(
        userId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) = Unit

    override suspend fun getProfile(userId: String): UserProfile? = null

    override suspend fun updateProfile(
        userId: String,
        displayName: String,
        avatarUrl: String?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) = Unit

    override suspend fun uploadProfileAvatar(
        userId: String,
        bytes: ByteArray,
        contentType: String,
    ): String = "https://example.com/avatar.jpg"

    fun completeGoogleSuccess() {
        checkNotNull(googleSuccess).invoke()
    }

    fun completeKakaoFailure(error: Exception) {
        checkNotNull(kakaoFailure).invoke(error)
    }
}
