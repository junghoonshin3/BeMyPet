package kr.sjh.bemypet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kr.sjh.data.repository.SettingRepository
import javax.inject.Inject

@HiltViewModel
class StartViewModel @Inject constructor(private val settingRepository: SettingRepository) :
    ViewModel() {

    val isDarkTheme = settingRepository.getDarkTheme()

    fun updateIsDarkTheme(isDarkTheme: Boolean) = viewModelScope.launch {
        settingRepository.updateIsDarkTheme(isDarkTheme)
    }
}