package kr.sjh.data.repository

import kotlinx.coroutines.flow.Flow
import kr.sjh.core.model.setting.Setting


interface SettingRepository {
    fun getSetting(): Flow<Setting>
    suspend fun insertSetting(setting: Setting)
    suspend fun isSettingExists(): Boolean
}