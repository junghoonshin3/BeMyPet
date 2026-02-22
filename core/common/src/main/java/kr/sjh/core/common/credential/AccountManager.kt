package kr.sjh.core.common.credential

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kr.sjh.core.common.BuildConfig
import java.security.MessageDigest
import java.util.UUID

class AccountManager(private val context: Context) {

    private val credentialManager = androidx.credentials.CredentialManager.create(context)

    suspend fun signIn(): SignInResult {
        val rawNonce = UUID.randomUUID().toString()
        val hashedNonce = hashNonce(rawNonce)
        val maskedClientId = maskClientId(BuildConfig.WEB_CLIENT_ID)

        Log.d(TAG, "Starting Google sign-in. clientIdSuffix=$maskedClientId")

        val request = buildSignInRequest(hashedNonce)
        return attemptSignIn(request = request, rawNonce = rawNonce, retryAttempted = false)
    }

    suspend fun signOut() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }

    private fun buildSignInRequest(hashedNonce: String): GetCredentialRequest {
        val signInOption = GetSignInWithGoogleOption.Builder(
            BuildConfig.WEB_CLIENT_ID
        ).setNonce(hashedNonce).build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()
    }

    private suspend fun attemptSignIn(
        request: GetCredentialRequest,
        rawNonce: String,
        retryAttempted: Boolean
    ): SignInResult {
        return try {
            val result = credentialManager.getCredential(
                request = request,
                context = context,
            )
            Log.d(TAG, "Credential response received. type=${result.credential.type}")
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            Log.d(TAG, "GoogleIdTokenCredential parsed successfully")
            Log.d(TAG, "Google sign-in success")
            SignInResult.Success(googleIdTokenCredential.idToken, rawNonce)
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "Google sign-in cancelled by user")
            SignInResult.Cancelled
        } catch (e: NoCredentialException) {
            if (!retryAttempted) {
                Log.d(TAG, "NoCredentialException. Clearing credential state and retrying once.")
                runCatching {
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                }.onFailure { clearError ->
                    Log.w(TAG, "Failed to clear credential state before retry.", clearError)
                }
                return attemptSignIn(request = request, rawNonce = rawNonce, retryAttempted = true)
            }
            Log.w(TAG, "NoCredentialException after retry.")
            SignInResult.NoCredentials
        } catch (e: GetCredentialException) {
            Log.w(TAG, "CredentialManager sign-in failure.", e)
            SignInResult.Failure(e)
        } catch (e: Exception) {
            Log.w(TAG, "Unexpected Google sign-in failure.", e)
            SignInResult.Failure(e)
        }
    }

    private fun hashNonce(rawNonce: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(rawNonce.toByteArray())
        return digest.fold("") { acc, byte -> acc + "%02x".format(byte) }
    }

    private fun maskClientId(clientId: String): String {
        if (clientId.isBlank()) return "(empty)"
        return "***${clientId.takeLast(10)}"
    }

    companion object {
        private const val TAG = "AccountManager"
    }
}

sealed interface SignInResult {
    data class Success(val idToken: String, val nonce: String) : SignInResult
    data object Cancelled : SignInResult
    data class Failure(val e: Exception) : SignInResult
    data object NoCredentials : SignInResult
}
