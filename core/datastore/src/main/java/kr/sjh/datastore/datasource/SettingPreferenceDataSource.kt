package kr.sjh.datastore.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.map
import kr.sjh.datastore.model.SettingsData
import javax.inject.Inject
import javax.inject.Named

class SettingPreferenceDataSource @Inject constructor(
    @Named("setting") private val dataStore: DataStore<Preferences>
) {
    object PreferencesKey {
        val IS_DARK_THEME = booleanPreferencesKey("IS_DARK_THEME")
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("HAS_SEEN_ONBOARDING")
        val PUSH_OPT_IN = booleanPreferencesKey("PUSH_OPT_IN")
    }

    val settingsData = dataStore.data.map { preferences ->
        SettingsData(
            isDarkTheme = preferences[PreferencesKey.IS_DARK_THEME] ?: false,
            hasSeenOnboarding = preferences[PreferencesKey.HAS_SEEN_ONBOARDING] ?: false,
            pushOptIn = preferences[PreferencesKey.PUSH_OPT_IN] ?: false
        )
    }

    suspend fun updateIsDarkTheme(isDarkTheme: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKey.IS_DARK_THEME] = isDarkTheme
        }
    }

    suspend fun updateHasSeenOnboarding(hasSeenOnboarding: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKey.HAS_SEEN_ONBOARDING] = hasSeenOnboarding
        }
    }

    suspend fun updatePushOptIn(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKey.PUSH_OPT_IN] = enabled
        }
    }
}
