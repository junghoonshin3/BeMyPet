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
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
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
        val normalizedUserId = userId.trim()
        if (normalizedUserId.isBlank()) {
            onFailure(IllegalArgumentException("유효하지 않은 사용자 정보예요."))
            return
        }
        val hasSession = auth.currentSessionOrNull() != null
        if (!hasSession) {
            onFailure(Exception(SESSION_EXPIRED_MESSAGE))
            return
        }

        try {
            invokeDeleteUser(normalizedUserId)
            onSuccess()
            return
        } catch (e: Exception) {
            val initialErrorMeta = parseDeleteAccountErrorMeta(e)

            if (shouldRetryDeleteAccount(initialErrorMeta)) {
                val refreshSucceeded = runCatching { auth.refreshCurrentSession() }.isSuccess
                if (refreshSucceeded) {
                    runCatching {
                        invokeDeleteUser(normalizedUserId)
                    }.onSuccess {
                        onSuccess()
                        return
                    }.onFailure { retryError ->
                        val retryErrorMeta = parseDeleteAccountErrorMeta(retryError)
                        onFailure(Exception(mapDeleteAccountErrorMessage(retryErrorMeta)))
                        return
                    }
                } else {
                    onFailure(Exception(SESSION_EXPIRED_MESSAGE))
                    return
                }
            }

            onFailure(Exception(mapDeleteAccountErrorMessage(initialErrorMeta)))
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

    private suspend fun invokeDeleteUser(userId: String) {
        client.functions.invoke(DELETE_USER_FUNCTION_NAME) {
            setBody("{\"user_id\": \"$userId\"}")
        }
    }

    private fun shouldRetryDeleteAccount(errorMeta: DeleteAccountErrorMeta): Boolean {
        return errorMeta.statusCode == 401 ||
            errorMeta.serverCode.equals(SERVER_CODE_UNAUTHORIZED, ignoreCase = true)
    }

    private fun parseDeleteAccountErrorMeta(error: Throwable): DeleteAccountErrorMeta {
        val restException = error as? RestException
        val errorPayload = restException?.error
        val serverCode = extractDeleteUserErrorField(errorPayload, "code")
        return DeleteAccountErrorMeta(
            statusCode = restException?.statusCode,
            serverCode = serverCode
        )
    }

    private fun extractDeleteUserErrorField(payload: String?, field: String): String? {
        val raw = payload?.trim().orEmpty()
        if (raw.isBlank()) return null
        return runCatching {
            Json.parseToJsonElement(raw).jsonObject[field]?.jsonPrimitive?.contentOrNull
        }.getOrNull()?.trim()?.takeIf { it.isNotBlank() }
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

    private data class DeleteAccountErrorMeta(
        val statusCode: Int? = null,
        val serverCode: String? = null,
    )

    companion object {
        private const val DELETE_USER_FUNCTION_NAME = "delete_user"
        private const val SERVER_CODE_UNAUTHORIZED = "UNAUTHORIZED"
        private const val SERVER_CODE_FORBIDDEN = "FORBIDDEN"
        private const val SERVER_CODE_PROFILE_UPDATE_FAILED = "PROFILE_UPDATE_FAILED"
        private const val SERVER_CODE_AUTH_DELETE_FAILED = "AUTH_DELETE_FAILED"
        private const val DELETE_ACCOUNT_NOT_READY_MESSAGE = "회원탈퇴 기능이 아직 준비되지 않았어요. 잠시 후 다시 시도해 주세요."
        private const val DELETE_ACCOUNT_FORBIDDEN_MESSAGE = "잘못된 요청이에요. 다시 시도해 주세요."
        private const val DELETE_ACCOUNT_PROCESS_FAILED_MESSAGE = "회원탈퇴 처리 중 오류가 발생했어요. 잠시 후 다시 시도해 주세요."
        private const val DELETE_ACCOUNT_FAILED_MESSAGE = "회원탈퇴에 실패했어요. 잠시 후 다시 시도해 주세요."
        private const val SESSION_EXPIRED_MESSAGE = "로그인 상태가 만료되었어요. 다시 로그인 후 시도해 주세요."

        private fun mapDeleteAccountErrorMessage(errorMeta: DeleteAccountErrorMeta): String {
            val code = errorMeta.serverCode.orEmpty().uppercase()
            return when {
                errorMeta.statusCode == 404 -> {
                    DELETE_ACCOUNT_NOT_READY_MESSAGE
                }

                errorMeta.statusCode == 401 || code == SERVER_CODE_UNAUTHORIZED -> {
                    SESSION_EXPIRED_MESSAGE
                }

                errorMeta.statusCode == 403 || code == SERVER_CODE_FORBIDDEN -> {
                    DELETE_ACCOUNT_FORBIDDEN_MESSAGE
                }

                errorMeta.statusCode == 500 ||
                    code == SERVER_CODE_PROFILE_UPDATE_FAILED ||
                    code == SERVER_CODE_AUTH_DELETE_FAILED -> {
                    DELETE_ACCOUNT_PROCESS_FAILED_MESSAGE
                }

                else -> {
                    DELETE_ACCOUNT_FAILED_MESSAGE
                }
            }
        }
    }
}
