package kr.sjh.core.google.impl

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kr.sjh.core.google.service.GoogleAuthService
import kr.sjh.core.model.google.AuthState
import javax.inject.Inject


class GoogleAuthServiceImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val googleIdOption: GetGoogleIdOption,
    private val credentialManager: CredentialManager
) : GoogleAuthService {

    override fun googleSignInUp(context: Context): Flow<AuthState<AuthResult>> = flow {
        emit(AuthState.Idle)
        try {
            val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

            val result = credentialManager.getCredential(context, request)
            emit(AuthState.Loading)

            when (val credential = result.credential) {
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        val firebaseCredentials = GoogleAuthProvider.getCredential(idToken, null)
                        val authResult = auth.signInWithCredential(firebaseCredentials).await()
                        emit(AuthState.Success(authResult))
                    } else {
                        throw IllegalArgumentException("Unsupported credential type: ${credential.type}")
                    }
                }

                else -> {
                    throw IllegalArgumentException("Unsupported credential: $credential")
                }
            }

        } catch (e: Exception) {
            emit(AuthState.Error(e))
        }

    }
}