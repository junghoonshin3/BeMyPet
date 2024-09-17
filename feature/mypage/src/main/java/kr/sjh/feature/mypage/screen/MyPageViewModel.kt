package kr.sjh.feature.mypage.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kr.sjh.core.firebase.service.AccountService
import kr.sjh.feature.mypage.state.NavigationState
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val accountService: AccountService
) : ViewModel() {

    private val _navigateState = MutableSharedFlow<NavigationState>()
    val navigationState = _navigateState.asSharedFlow()

    fun signOut() {
        viewModelScope.launch(Dispatchers.IO) {
            accountService.signOut()
            _navigateState.emit(NavigationState.NavigationToLogin)
        }

    }
}