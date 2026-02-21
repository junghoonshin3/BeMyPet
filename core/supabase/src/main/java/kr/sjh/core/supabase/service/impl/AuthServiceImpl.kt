package kr.sjh.core.supabase.service.impl

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
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
import io.github.jan.supabase.realtime.PrimaryKey
import io.github.jan.supabase.realtime.selectSingleValueAsFlow
import io.ktor.client.request.setBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kr.sjh.core.model.Role
import kr.sjh.core.model.SessionState
import kr.sjh.core.model.User
import kr.sjh.core.model.UserProfile
import kr.sjh.core.supabase.service.AuthService
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject

class AuthServiceImpl @Inject constructor(
    private val auth: Auth, private val client: SupabaseClient
) : AuthService {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val sharedSessionFlow = auth.sessionStatus
        .transformLatest { result ->
            when (result) {
                is SessionStatus.Authenticated -> {
                    emitAuthenticatedSession(result)
                }

                SessionStatus.Initializing -> {
                    Log.d("sjh", "Initializing")
                    emit(SessionState.Initializing)
                }

                is SessionStatus.NotAuthenticated -> {
                    Log.d("sjh", "NotAuthenticated")
                    emit(SessionState.NoAuthenticated(result.isSignOut))
                }

                is SessionStatus.RefreshFailure -> {
                    Log.d("sjh", "RefreshFailure")
                    emit(SessionState.RefreshFailure)
                }
            }
        }
        .catch { e ->
            if (e is CancellationException) throw e
            logSafeError("Session stream failed. Falling back to not authenticated.", e)
            emit(SessionState.NoAuthenticated(isSignOut = false))
        }
        .stateIn(
            scope = serviceScope,
            started = SharingStarted.Eagerly,
            initialValue = SessionState.Initializing
        )

    override suspend fun signInWithGoogle(
        idToken: String, rawNonce: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit
    ) {
        Log.d(TAG, "signInWithGoogle start")
        try {
            withTimeout(SIGN_IN_TIMEOUT_MS) {
                auth.signInWith(IDToken) {
                    this.idToken = idToken
                    provider = Google
                    nonce = rawNonce
                }
            }
            Log.d(TAG, "signInWithGoogle success")
            onSuccess()
        } catch (e: TimeoutCancellationException) {
            logSafeError("signInWithGoogle timeout", e)
            onFailure(Exception("로그인 응답 시간이 초과되었어요. 네트워크 상태를 확인해 주세요."))
        } catch (e: AuthRestException) {
            val safeMessage = e.errorDescription?.takeIf { it.isNotBlank() }
                ?: "Google 로그인에 실패했어요. 잠시 후 다시 시도해 주세요."
            logSafeError("signInWithGoogle AuthRestException: $safeMessage", e)
            onFailure(Exception(safeMessage))
        } catch (e: Exception) {
            val safeMessage = e.message?.takeIf { it.isNotBlank() }
                ?: "Google 로그인에 실패했어요. 잠시 후 다시 시도해 주세요."
            logSafeError("signInWithGoogle Exception", e)
            onFailure(Exception(safeMessage))
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

        val existed = runCatching { getProfile(userId) }.onFailure {
            logSafeError("profiles unavailable, using fallback profile", it)
        }.getOrNull()
        if (existed != null) {
            return existed
        }

        runCatching {
            client.from("profiles").upsert(
                mapOf(
                    "user_id" to userId,
                    "display_name" to fallbackName,
                    "avatar_url" to fallbackAvatar
                )
            )
        }.onFailure {
            logSafeError("profiles upsert failed, using fallback profile", it)
        }

        return runCatching { getProfile(userId) }.onFailure {
            logSafeError("profiles refetch failed, using fallback profile", it)
        }.getOrNull() ?: UserProfile(
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

    override fun getSessionFlow(): Flow<SessionState> = sharedSessionFlow

    @OptIn(SupabaseExperimental::class)
    private suspend fun kotlinx.coroutines.flow.FlowCollector<SessionState>.emitAuthenticatedSession(
        authenticated: SessionStatus.Authenticated
    ) {
        val userInfo = authenticated.session.user
        val id = userInfo?.id?.toString().orEmpty()
        val rawUserMetaData = userInfo?.userMetadata ?: JsonObject(emptyMap())
        val fallbackProfile = UserProfile(
            userId = id,
            displayName = buildFallbackDisplayName(
                userId = id,
                preferred = rawUserMetaData["name"]?.jsonPrimitive?.contentOrNull
                    ?: rawUserMetaData["full_name"]?.jsonPrimitive?.contentOrNull
            ),
            avatarUrl = rawUserMetaData["avatar_url"]?.jsonPrimitive?.contentOrNull
        )

        val profile = runCatching { ensureProfile(userInfo) }.onFailure {
            logSafeError("profiles unavailable, using fallback profile", it)
        }.getOrElse { fallbackProfile }

        val role = runCatching { getRole(id) }.onFailure {
            logSafeError("role lookup failed, defaulting to USER", it)
        }.getOrDefault(Role.USER)

        val initialBanRow = runCatching { getBanRow(id) }.onFailure {
            logSafeError("Initial banned_until lookup failed. Continuing with fallback.", it)
        }.getOrNull()

        var lastIsBanned = isBannedNow(initialBanRow?.bannedUntil)
        logBanState(
            userId = id,
            isBanned = lastIsBanned,
            bannedUntil = initialBanRow?.bannedUntil
        )

        emitAuthenticatedState(
            id = id,
            profile = profile,
            rawUserMetaData = rawUserMetaData,
            role = role,
            bannedUntil = initialBanRow?.bannedUntil,
            isBanned = lastIsBanned
        )

        if (id.isBlank()) {
            Log.w(TAG, "ban_realtime_subscribe_skip reason=blank_user_id")
            return
        }

        try {
            observeBanRealtime(userId = id) { row ->
                val currentIsBanned = isBannedNow(row.bannedUntil)
                if (currentIsBanned != lastIsBanned) {
                    logBanState(
                        userId = id,
                        isBanned = currentIsBanned,
                        bannedUntil = row.bannedUntil
                    )
                    lastIsBanned = currentIsBanned
                }

                emitAuthenticatedState(
                    id = id,
                    profile = profile,
                    rawUserMetaData = rawUserMetaData,
                    role = role,
                    bannedUntil = row.bannedUntil,
                    isBanned = currentIsBanned
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logSafeError("Ban realtime observation stopped. Keeping last session state.", e)
        }
    }

    private suspend fun kotlinx.coroutines.flow.FlowCollector<SessionState>.emitAuthenticatedState(
        id: String,
        profile: UserProfile,
        rawUserMetaData: JsonObject,
        role: Role,
        bannedUntil: String?,
        isBanned: Boolean
    ) {
        emit(
            SessionState.Authenticated(
                User(
                    id = id,
                    displayName = profile.displayName,
                    avatarUrl = profile.avatarUrl,
                    rawUserMetaData = rawUserMetaData,
                    role = role,
                    bannedUntil = bannedUntil ?: "알수없음",
                    isBanned = isBanned
                )
            )
        )
    }

    @OptIn(SupabaseExperimental::class)
    private suspend fun observeBanRealtime(
        userId: String,
        onRow: suspend (BanProfileRow) -> Unit
    ) {
        val userSuffix = userId.takeLast(6).ifBlank { "unknown" }
        var retried = false

        client.from("profiles").selectSingleValueAsFlow(
            primaryKey = PrimaryKey<BanProfileRow>("user_id") { it.userId },
            channelName = "ban-profile:$userId",
        ) {
            eq("user_id", userId)
        }.onStart {
            Log.i(TAG, "ban_realtime_subscribe_start user=*${userSuffix}")
        }.retryWhen { cause, attempt ->
            if (cause is CancellationException) throw cause

            val delayMillis = calculateRetryDelayMillis(attempt)
            retried = true
            Log.w(
                TAG,
                "ban_realtime_subscribe_retry user=*${userSuffix} attempt=${attempt + 1} delay=${delayMillis}ms (${cause.javaClass.simpleName})"
            )
            delay(delayMillis)
            true
        }.collect { row ->
            if (retried) {
                Log.i(TAG, "ban_realtime_subscribe_recovered user=*${userSuffix}")
                retried = false
            }
            onRow(row)
        }
    }

    private fun calculateRetryDelayMillis(attempt: Long): Long {
        return when (attempt.coerceAtMost(3L).toInt()) {
            0 -> 1_000L
            1 -> 2_000L
            2 -> 4_000L
            else -> 10_000L
        }
    }

    private suspend fun getBanRow(userId: String): BanProfileRow? {
        return client.from("profiles").select(Columns.raw("user_id, banned_until")) {
            filter {
                eq("user_id", userId)
            }
        }.decodeList<BanProfileRow>().firstOrNull()
    }

    private fun isBannedNow(bannedUntil: String?): Boolean {
        return isBannedNowForTest(bannedUntil = bannedUntil, now = Instant.now())
    }

    private fun logBanState(userId: String, isBanned: Boolean, bannedUntil: String?) {
        val suffix = userId.takeLast(6).ifBlank { "unknown" }
        val safeUntil = bannedUntil?.takeIf { it.isNotBlank() } ?: "null"
        Log.i(TAG, "ban_state user=*${suffix}, isBanned=$isBanned, bannedUntil=$safeUntil")
    }

    private fun logSafeError(message: String, throwable: Throwable?) {
        val throwableType = throwable?.javaClass?.simpleName ?: "Unknown"
        Log.e(TAG, "$message ($throwableType)")
    }

    @Serializable
    private data class BanProfileRow(
        @SerialName("user_id") val userId: String,
        @SerialName("banned_until") val bannedUntil: String? = null,
    )

    companion object {
        private const val TAG = "AuthServiceImpl"
        private const val SIGN_IN_TIMEOUT_MS = 15_000L

        internal fun isBannedNowForTest(bannedUntil: String?, now: Instant): Boolean {
            val bannedUntilInstant = parseBannedUntilInstantForTest(bannedUntil) ?: return false
            return bannedUntilInstant.isAfter(now)
        }

        internal fun parseBannedUntilInstantForTest(bannedUntil: String?): Instant? {
            val raw = bannedUntil?.trim().orEmpty()
            if (raw.isBlank()) return null

            val normalized = raw.replace(' ', 'T')
            return runCatching { Instant.parse(normalized) }.getOrNull()
                ?: runCatching { OffsetDateTime.parse(normalized).toInstant() }.getOrNull()
                ?: runCatching {
                    LocalDateTime.parse(normalized).toInstant(ZoneOffset.UTC)
                }.getOrElse {
                    runCatching {
                        Log.w(TAG, "ban_until_parse_failed value=${raw.take(64)}")
                    }
                    null
                }
        }
    }
}
