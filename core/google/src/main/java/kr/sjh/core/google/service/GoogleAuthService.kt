package kr.sjh.core.google.service

import android.content.Context
import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.flow.Flow
import kr.sjh.core.model.google.AuthState

interface GoogleAuthService {
    fun googleSignInUp(context: Context): Flow<AuthState<AuthResult>>

}