package kr.sjh.data.repository.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import kr.sjh.core.model.SessionState
import kr.sjh.core.model.UserProfile
import kr.sjh.core.supabase.service.AuthService
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthRepositoryImplUploadAvatarTest {

    @Test
    fun uploadProfileAvatar_delegatesToAuthService() = runBlocking {
        val authService = FakeAuthService()
        val repository = AuthRepositoryImpl(authService)
        val bytes = byteArrayOf(1, 2, 3)

        val result = repository.uploadProfileAvatar(
            userId = "user-id",
            bytes = bytes,
            contentType = "image/jpeg",
        )

        assertEquals("https://example/avatar.jpg", result)
        assertEquals("user-id", authService.lastUserId)
        assertArrayEquals(bytes, authService.lastBytes)
        assertEquals("image/jpeg", authService.lastContentType)
    }

    private class FakeAuthService : AuthService {
        var lastUserId: String? = null
        var lastBytes: ByteArray? = null
        var lastContentType: String? = null

        override suspend fun uploadProfileAvatar(
            userId: String,
            bytes: ByteArray,
            contentType: String,
        ): String {
            lastUserId = userId
            lastBytes = bytes
            lastContentType = contentType
            return "https://example/avatar.jpg"
        }

        override suspend fun signInWithGoogle(
            idToken: String,
            rawNonce: String,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit,
        ) = Unit

        override suspend fun signInWithKakao(
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
        ) = Unit
    }
}
