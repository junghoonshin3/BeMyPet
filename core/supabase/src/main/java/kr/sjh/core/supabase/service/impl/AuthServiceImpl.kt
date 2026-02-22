package kr.sjh.core.supabase.service.impl

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SignOutScope
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kr.sjh.core.model.Role
import kr.sjh.core.model.SessionState
import kr.sjh.core.model.User
import kr.sjh.core.model.UserProfile
import kr.sjh.core.supabase.service.AuthService
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
        return runCatching {
            val result = client.from("user_roles").select(Columns.raw("role")) {
                filter {
                    eq("user_id", userId)
                }
            }.decodeSingle<Map<String, String>>()
            val role = result["role"]?.lowercase()
            if (role == "admin") {
                Role.ADMIN
            } else {
                Role.USER
            }
        }.getOrDefault(Role.USER)
    }

    override suspend fun getProfile(userId: String): UserProfile? {
        return runCatching {
            client.from("profiles").select {
                filter {
                    eq("user_id", userId)
                    eq("is_deleted", false)
                }
            }.decodeSingle<UserProfile>()
        }.getOrNull()
    }

    override suspend fun updateProfile(
        userId: String,
        displayName: String,
        avatarUrl: String?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            client.from("profiles").update(
                mapOf(
                    "display_name" to displayName.trim(),
                    "avatar_url" to avatarUrl?.trim()?.ifBlank { null }
                )
            ) {
                filter {
                    eq("user_id", userId)
                }
            }
            onSuccess()
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    private suspend fun ensureProfile(user: UserInfo?): UserProfile {
        val userId = user?.id?.toString().orEmpty()
        val meta = user?.userMetadata ?: JsonObject(emptyMap())
        val fallbackName = buildFallbackDisplayName(
            userId = userId,
            preferred = meta["name"]?.jsonPrimitive?.contentOrNull
                ?: meta["full_name"]?.jsonPrimitive?.contentOrNull
        )
        val fallbackAvatar = meta["avatar_url"]?.jsonPrimitive?.contentOrNull

        val existed = getProfile(userId)
        if (existed != null) {
            return existed
        }

        client.from("profiles").upsert(
            mapOf(
                "user_id" to userId,
                "display_name" to fallbackName,
                "avatar_url" to fallbackAvatar
            )
        )

        return getProfile(userId) ?: UserProfile(
            userId = userId,
            displayName = fallbackName,
            avatarUrl = fallbackAvatar
        )
    }

    private fun buildFallbackDisplayName(userId: String, preferred: String?): String {
        val normalizedPreferred = preferred?.trim().orEmpty()
        if (normalizedPreferred.isNotBlank()) {
            return "${normalizedPreferred}_${userId.takeLast(6)}"
        }
        return "user_${userId.takeLast(6)}"
    }

    override fun getSessionFlow() = auth.sessionStatus.map { result ->
        when (result) {
            is SessionStatus.Authenticated -> {
                val userInfo = result.session.user
                val id = userInfo?.id.toString()
                val rawUserMetaData = userInfo?.userMetadata ?: JsonObject(emptyMap())
                val profile = ensureProfile(userInfo)
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
                        displayName = profile.displayName,
                        avatarUrl = profile.avatarUrl,
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
