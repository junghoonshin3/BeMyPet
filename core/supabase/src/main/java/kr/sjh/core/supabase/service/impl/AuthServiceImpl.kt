package kr.sjh.core.supabase.service.impl

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SignOutScope
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive
import kr.sjh.core.model.Role
import kr.sjh.core.model.SessionState
import kr.sjh.core.model.User
import kr.sjh.core.supabase.service.AuthService
import java.util.Locale
import javax.inject.Inject

class AuthServiceImpl @Inject constructor(
    private val auth: Auth, private val client: SupabaseClient
) : AuthService {

    override suspend fun signInWithGoogle(
        idToken: String, rawNonce: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit
    ) {
        try {
            auth.signInWith(IDToken) {
                this.idToken = idToken
                provider = Google
                nonce = rawNonce
            }
            onSuccess()
        } catch (e: Exception) {
            when (e) {
                is AuthRestException -> {
                    onFailure(Exception(e.errorDescription))
                }

                else -> {
                    onFailure(e)
                }
            }
        }
    }

    override suspend fun deleteAccount(
        userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit
    ) {
        try {
            client.functions.invoke("delete_user") {
                setBody(
                    "{\"user_id\": \"$userId\"}"
                )
            }
            onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            onFailure(e)
        }

    }


    override suspend fun signOut() {
        auth.signOut(SignOutScope.LOCAL)
    }

    private suspend fun getRole(userId: String): Role {
        val result = client.from("user_roles").select(Columns.raw("role")) {
            filter {
                eq("user_id", userId)
            }
        }.decodeSingle<Map<String, String>>()
        val role = result["role"]?.lowercase()
        return if (role == "admin") {
            Role.ADMIN
        } else {
            Role.USER
        }
    }

    override fun getSessionFlow() = auth.sessionStatus.map { result ->
        when (result) {
            is SessionStatus.Authenticated -> {
                val userInfo = result.session.user
                val id = userInfo?.id.toString()
                val rawUserMetaData = userInfo?.userMetadata ?: JsonObject(emptyMap())
                val bannedObj = client.functions.invoke("banned_until") {
                    setBody(
                        "{\"user_id\": \"$id\"}"
                    )
                }.body<JsonObject>()

                val isBanned = bannedObj["isBanned"]?.jsonPrimitive?.booleanOrNull ?: false
                Log.d("sjh", "isBanned : ${isBanned}")

                val bannedUntil = bannedObj["bannedUntil"]?.jsonPrimitive?.content ?: "알수없음"
                Log.d("sjh", "bannedUntil : $bannedUntil")

                if (isBanned) {
                    auth.sessionManager.deleteSession()
                    return@map SessionState.Banned(bannedUntil)
                }

                val role = getRole(id)

                SessionState.Authenticated(
                    User(
                        id = id,
                        rawUserMetaData = rawUserMetaData,
                        role = role,
                        bannedUntil = bannedUntil,
                        isBanned = isBanned
                    )
                )
            }

            SessionStatus.Initializing -> {
                Log.d("sjh", "Initializing")

                SessionState.Initializing
            }

            is SessionStatus.NotAuthenticated -> {
                Log.d("sjh", "NotAuthenticated")
                SessionState.NoAuthenticated(result.isSignOut)
            }

            is SessionStatus.RefreshFailure -> {
                Log.d("sjh", "RefreshFailure")
                SessionState.RefreshFailure
            }
        }
    }
}
