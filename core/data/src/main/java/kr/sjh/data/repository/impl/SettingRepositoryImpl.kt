package kr.sjh.data.repository.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kr.sjh.data.repository.SettingRepository
import kr.sjh.datastore.datasource.SettingPreferenceDataSource
import javax.inject.Inject

class SettingRepositoryImpl @Inject constructor(private val preferencesDataSource: SettingPreferenceDataSource) :
    SettingRepository {
    override fun getDarkTheme(): Flow<Boolean> = preferencesDataSource.settingsData.map {
        it.isDarkTheme
    }

    override fun getHasSeenOnboarding(): Flow<Boolean> = preferencesDataSource.settingsData.map {
        it.hasSeenOnboarding
    }

    override suspend fun updateIsDarkTheme(isDarkTheme: Boolean) {
        preferencesDataSource.updateIsDarkTheme(isDarkTheme)
    }

    override suspend fun updateHasSeenOnboarding(seen: Boolean) {
        preferencesDataSource.updateHasSeenOnboarding(seen)
    }
}
