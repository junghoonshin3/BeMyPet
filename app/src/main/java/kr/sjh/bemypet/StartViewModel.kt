package kr.sjh.bemypet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.Auth
import kotlinx.coroutines.launch
import kr.sjh.data.repository.SettingRepository
import javax.inject.Inject

@HiltViewModel
class StartViewModel @Inject constructor(
    private val settingRepository: SettingRepository, private val auth: Auth
) : ViewModel() {

    val isDarkTheme = settingRepository.getDarkTheme()

    fun updateIsDarkTheme(isDarkTheme: Boolean) = viewModelScope.launch {
        settingRepository.updateIsDarkTheme(isDarkTheme)
    }

//    private fun getSession() {
//        viewModelScope.launch {
//            val userInfo = auth.sessionManager.loadSession()?.user
//            if (userInfo == null) {
//                _user.value = null
//            } else {
//                Log.d("sjh", "userInfo: ${userInfo.userMetadata?.toString()}")
//                _user.update {
//                    UserModel(
//                        id = userInfo.id,
//                        nickname = userInfo.userMetadata?.get("name").toString(),
//                        email = userInfo.email.toString(),
//                        profileImageUrl = userInfo.userMetadata?.get("avatar_url").toString()
//                    )
//                }
//            }
//        }
//
//    }

}