package kr.sjh.core.common.credential

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kr.sjh.core.common.BuildConfig
import java.security.MessageDigest
import java.util.UUID

class AccountManager(private val context: Context) {

    private val credentialManager = androidx.credentials.CredentialManager.create(context)

    suspend fun signIn(filterByAuthorizedAccounts: Boolean): SignInResult {
        return try {
            // Generate a nonce and hash it with sha-256
            // Providing a nonce is optional but recommended
            val rawNonce = UUID.randomUUID()
                .toString() // Generate a random String. UUID should be sufficient, but can also be any other random string.
            val bytes = rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce =
                digest.fold("") { str, it -> str + "%02x".format(it) } // Hashed nonce to be passed to Google sign-in

            val googleIdOption: GetGoogleIdOption =
                GetGoogleIdOption.Builder().setServerClientId(BuildConfig.WEB_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
                    .setAutoSelectEnabled(true)
                    .setNonce(hashedNonce) // Provide the nonce if you have one
                    .build()

            val request: GetCredentialRequest =
                GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

            val result = credentialManager.getCredential(
                request = request,
                context = context,
            )
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            SignInResult.Success(googleIdTokenCredential.idToken, rawNonce)
        } catch (e: GetCredentialCancellationException) {
            e.printStackTrace()
            SignInResult.Cancelled
        } catch (e: NoCredentialException) {
            e.printStackTrace()
            SignInResult.NoCredentials
        } catch (e: GetCredentialException) {
            e.printStackTrace()
            SignInResult.Failure(e)
        } catch (e: Exception) {
            e.printStackTrace()
            SignInResult.Failure(e)
        }
    }

    suspend fun signOut() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
}

sealed interface SignInResult {
    data class Success(val idToken: String, val nonce: String) : SignInResult
    data object Cancelled : SignInResult
    data class Failure(val e: Exception) : SignInResult
    data object NoCredentials : SignInResult
}