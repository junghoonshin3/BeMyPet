package kr.sjh.setting.screen

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kr.sjh.core.model.SessionState
import kr.sjh.core.model.UserProfile
import kr.sjh.data.repository.AuthRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class SettingViewModelProfileUploadTest {

    @get:Rule
    private val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun updateProfileWithAvatar_uploadsFirst_thenUpdatesProfile() = runTest {
        val fakeRepository = FakeAuthRepository()
        val viewModel = SettingViewModel(fakeRepository)
        var failure: Exception? = null

        viewModel.updateProfileWithAvatar(
            userId = "user-id",
            displayName = "new-name",
            avatarBytes = byteArrayOf(1, 2),
            currentAvatarUrl = "https://example.com/old-avatar.jpg",
            onSuccess = {},
            onFailure = { failure = it },
        )
        advanceUntilIdle()

        assertEquals(listOf("upload", "update"), fakeRepository.callOrder)
        assertNull(failure)
    }
}

private class FakeAuthRepository : AuthRepository {
    val callOrder = mutableListOf<String>()

    override suspend fun signInWithGoogle(
        idToken: String,
        rawNonce: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) = Unit

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
    ) {
        callOrder += "update"
        onSuccess()
    }

    override suspend fun uploadProfileAvatar(
        userId: String,
        bytes: ByteArray,
        contentType: String,
    ): String {
        callOrder += "upload"
        return "https://example.com/avatar.jpg"
    }
}

private class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
