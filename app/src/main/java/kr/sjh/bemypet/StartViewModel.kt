package kr.sjh.bemypet

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kr.sjh.core.firebase.service.AccountService
import kr.sjh.core.firebase.service.FireStoreRepository
import javax.inject.Inject

enum class AccountState {
    Loading, UserAlreadySignIn, UserNotSignIn,
}

@HiltViewModel
class StartViewModel @Inject constructor(
    private val accountService: AccountService, private val firestoreRepository: FireStoreRepository
) : ViewModel() {

    private val _accountState = MutableStateFlow(AccountState.Loading)
    val accountState = _accountState.asStateFlow()

    private var initializeCalled = false

    @MainThread
    fun initialize() {
        if (initializeCalled) return
        initializeCalled = true
        viewModelScope.launch {
            runCatching {
                accountService.firebaseUser?.email?.let { email ->
                    val isUserExist = firestoreRepository.isUserInDatabase(email).getOrThrow()
                    if (isUserExist) {
                        _accountState.update { AccountState.UserAlreadySignIn }
                    } else {
                        _accountState.update { AccountState.UserNotSignIn }
                    }
                } ?: let {
                    _accountState.update { AccountState.UserNotSignIn }
                }
            }.onFailure {
                _accountState.update { AccountState.UserNotSignIn }
            }
        }
    }

}