package kr.sjh.core.supabase.service

import kotlinx.coroutines.flow.Flow
import kr.sjh.core.model.SessionState

interface AuthService {
    suspend fun signInWithGoogle(
        idToken: String, rawNonce: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit
    )

    suspend fun signOut()

    fun getSessionFlow(): Flow<SessionState>

    suspend fun deleteAccount(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}