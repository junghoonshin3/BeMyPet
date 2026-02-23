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
import kr.sjh.core.model.setting.SettingType
import kr.sjh.data.repository.AuthRepository
import kr.sjh.data.repository.SettingRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
        val settingRepository = FakeSettingRepository()
        val viewModel = SettingViewModel(fakeRepository, settingRepository)

        viewModel.updateProfileWithAvatar(
            userId = "user-id",
            displayName = "new-name",
            avatarBytes = byteArrayOf(1, 2),
            currentAvatarUrl = "https://example.com/old-avatar.jpg",
        )
        advanceUntilIdle()

        assertEquals(listOf("upload", "update"), fakeRepository.callOrder)
    }

    @Test
    fun startProfileEdit_andDismiss_keepsStateInViewModel() {
        val viewModel = SettingViewModel(FakeAuthRepository(), FakeSettingRepository())

        viewModel.startProfileEdit(
            userId = "user-id",
            displayName = "홍길동",
            currentAvatarUrl = "https://example.com/a.jpg"
        )
        val afterStart = viewModel.profileUiState.value.profileEditDraft
        assertTrue(afterStart.isVisible)
        assertEquals("홍길동", afterStart.nameInput)
        assertEquals("https://example.com/a.jpg", afterStart.originalAvatarUrlForSave)

        viewModel.dismissProfileEdit(clearDraft = true)
        assertNull(viewModel.profileUiState.value.profileEditDraft.editingUserId)
    }

    @Test
    fun selectTheme_updatesUiStateTheme() {
        val viewModel = SettingViewModel(FakeAuthRepository(), FakeSettingRepository())

        viewModel.selectTheme(SettingType.DARK_THEME)

        assertEquals(SettingType.DARK_THEME, viewModel.profileUiState.value.selectedTheme)
    }

    @Test
    fun setPushOptIn_updatesUiStateImmediately() {
        val viewModel = SettingViewModel(FakeAuthRepository(), FakeSettingRepository(initialPushOptIn = true))

        viewModel.setPushOptIn(false)

        assertFalse(viewModel.profileUiState.value.pushOptIn)
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

private class FakeSettingRepository(initialPushOptIn: Boolean = true) : SettingRepository {
    private val pushOptIn = kotlinx.coroutines.flow.MutableStateFlow(initialPushOptIn)
    var lastUpdatedPushOptIn: Boolean = initialPushOptIn

    override fun getDarkTheme(): Flow<Boolean> = emptyFlow()

    override fun getHasSeenOnboarding(): Flow<Boolean> = emptyFlow()

    override fun getPushOptIn(): Flow<Boolean> = pushOptIn

    override suspend fun updateIsDarkTheme(isDarkTheme: Boolean) = Unit

    override suspend fun updateHasSeenOnboarding(seen: Boolean) = Unit

    override suspend fun updatePushOptIn(enabled: Boolean) {
        lastUpdatedPushOptIn = enabled
        pushOptIn.value = enabled
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
