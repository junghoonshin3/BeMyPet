package kr.sjh.core.google.impl

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kr.sjh.core.google.service.GoogleProfileService
import kr.sjh.core.google.service.RevokeAccessResponse
import kr.sjh.core.google.service.SignOutResponse
import kr.sjh.core.model.google.GoogleResponse
import javax.inject.Inject

class GoogleProfileServiceImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val credentialManager: CredentialManager,
) : GoogleProfileService {
    override val displayName = auth.currentUser?.displayName.toString()
    override val photoUrl = auth.currentUser?.photoUrl.toString()

    override suspend fun signOut(): SignOutResponse {
        return try {
            val request = ClearCredentialStateRequest()
            credentialManager.clearCredentialState(request)
            auth.signOut()
            GoogleResponse.Success(data = true)
        } catch (e: Exception) {
            GoogleResponse.Failure(e)
        }
    }

    override suspend fun revokeAccess(): RevokeAccessResponse {
        return try {
            val request = ClearCredentialStateRequest()
            credentialManager.clearCredentialState(request)
            auth.currentUser?.apply {
                delete().await()
            }
            GoogleResponse.Success(data = true)
        } catch (e: ApiException) {
            GoogleResponse.Failure(e)
        }
    }
}
