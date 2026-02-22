package kr.sjh.datastore.model

data class SettingsData(
    val isDarkTheme: Boolean = false,
    val hasSeenOnboarding: Boolean = false,
    val pushOptIn: Boolean = true,
)
