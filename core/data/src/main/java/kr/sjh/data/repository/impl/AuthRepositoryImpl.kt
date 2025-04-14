package kr.sjh.data.repository.impl

import kr.sjh.core.model.User
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

}