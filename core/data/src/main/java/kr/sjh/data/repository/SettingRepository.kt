package kr.sjh.data.repository

import kotlinx.coroutines.flow.Flow


interface SettingRepository {
    fun getDarkTheme(): Flow<Boolean>
    fun getHasSeenOnboarding(): Flow<Boolean>
    fun getPushOptIn(): Flow<Boolean>
    suspend fun updateIsDarkTheme(isDarkTheme: Boolean)
    suspend fun updateHasSeenOnboarding(seen: Boolean)
    suspend fun updatePushOptIn(enabled: Boolean)
}
