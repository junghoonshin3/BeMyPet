package kr.sjh.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kr.sjh.core.model.google.AuthState

interface AuthenticationRepository {
    fun signInUpWithGoogle(context: Context): Flow<AuthState<FirebaseUser>>
}