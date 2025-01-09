package kr.sjh.setting.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kr.sjh.core.model.setting.Setting
import kr.sjh.data.repository.SettingRepository
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(private val settingRepository: SettingRepository) :
    ViewModel() {

    init {
        viewModelScope.launch {
            if (!settingRepository.isSettingExists()) {
                settingRepository.insertSetting(Setting())
            }
        }
    }

    val setting = settingRepository.getSetting()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), Setting())

    fun changeSetting(setting: Setting) {
        viewModelScope.launch {
            settingRepository.insertSetting(setting)
        }
    }
}