package kr.sjh.core.supabase.service.impl

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SignOutScope
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.safeBody
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonObject
import kr.sjh.core.model.SessionState
import kr.sjh.core.model.User
import kr.sjh.core.supabase.service.AuthService
import javax.inject.Inject

class AuthServiceImpl @Inject constructor(
    private val auth: Auth, private val client: SupabaseClient
) : AuthService {
    override suspend fun signInWithGoogle(
        idToken: String, rawNonce: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit
    ) {
        Log.d(
            "sjh", """
                idToken: $idToken
                rawNonce: $rawNonce
        """.trimMargin()
        )
        try {
            auth.signInWith(IDToken) {
                this.idToken = idToken
                provider = Google
                nonce = rawNonce
            }
            onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            onFailure(e)
        }
    }

    override suspend fun deleteAccount(
        userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit
    ) {
        try {
            Log.d(
                "sjh", """
                userId: $userId
        """.trimMargin()
            )
            val result = client.functions.invoke("delete_user") {
                setBody(
                    """
                {"user_id": "$userId"}
            """.trimIndent()
                )
            }
            if (result.status.isSuccess()) {
                onSuccess()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onFailure(e)
        }


    }


    override suspend fun signOut() {
        auth.signOut(SignOutScope.LOCAL)
    }

    override fun getSessionFlow() = auth.sessionStatus.map { result ->
        when (result) {
            is SessionStatus.Authenticated -> {
                val user = result.session.user
                SessionState.Authenticated(
                    User(
                        id = user?.id.toString(),
                        rawUserMetaData = user?.userMetadata ?: JsonObject(
                            emptyMap()
                        )
                    )
                )
            }

            SessionStatus.Initializing -> {
                SessionState.Initializing
            }

            is SessionStatus.NotAuthenticated -> {
                SessionState.NoAuthenticated(result.isSignOut)
            }

            is SessionStatus.RefreshFailure -> {
                SessionState.RefreshFailure
            }
        }
    }
}
