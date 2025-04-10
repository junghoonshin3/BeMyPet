package kr.sjh.setting.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kr.sjh.data.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(private val authRepository: AuthRepository) :
    ViewModel() {

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun deleteAccount(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            authRepository.deleteAccount(userId, onSuccess, onFailure)
        }
    }


}