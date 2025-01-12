package kr.sjh.data.repository

import kotlinx.coroutines.flow.Flow


interface SettingRepository {
    fun getDarkTheme(): Flow<Boolean>
    suspend fun updateIsDarkTheme(isDarkTheme: Boolean)
}