package kr.sjh.data.repository

import kotlinx.coroutines.flow.Flow
import kr.sjh.core.model.SessionState
import kr.sjh.core.model.UserProfile


interface AuthRepository {
    suspend fun signInWithGoogle(
        idToken: String, rawNonce: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit
    )

    suspend fun signInWithKakao(
        onSuccess: () -> Unit, onFailure: (Exception) -> Unit
    )

    suspend fun signOut()

    fun getSessionFlow(): Flow<SessionState>

    suspend fun deleteAccount(
        userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit
    )

    suspend fun getProfile(userId: String): UserProfile?

    suspend fun updateProfile(
        userId: String,
        displayName: String,
        avatarUrl: String?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )

    suspend fun uploadProfileAvatar(
        userId: String,
        bytes: ByteArray,
        contentType: String = "image/jpeg",
    ): String
}
