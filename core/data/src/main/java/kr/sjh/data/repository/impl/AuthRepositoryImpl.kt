package kr.sjh.data.repository.impl

import kr.sjh.core.supabase.service.AuthService
import kr.sjh.data.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
) : AuthRepository {

    override suspend fun signInWithGoogle(
        idToken: String,
        rawNonce: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        authService.signInWithGoogle(idToken, rawNonce, onSuccess, onFailure)
    }

    override suspend fun signOut() {
        authService.signOut()
    }

    override fun getSessionFlow() = authService.getSessionFlow()

    override suspend fun deleteAccount(
        userId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        authService.deleteAccount(userId, onSuccess, onFailure)
    }

    override suspend fun getProfile(userId: String) = authService.getProfile(userId)

    override suspend fun updateProfile(
        userId: String,
        displayName: String,
        avatarUrl: String?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        authService.updateProfile(userId, displayName, avatarUrl, onSuccess, onFailure)
    }

}
