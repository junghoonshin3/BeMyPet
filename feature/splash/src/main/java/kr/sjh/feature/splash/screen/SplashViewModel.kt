package kr.sjh.feature.splash.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kr.sjh.feature.firebase.service.AccountService
import kr.sjh.feature.firebase.service.FireStoreRepository
import kr.sjh.feature.splash.state.AccountState
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val accountService: AccountService, private val firestoreRepository: FireStoreRepository
) : ViewModel() {

    private val _accountState = MutableStateFlow(AccountState.Loading)
    val accountState = _accountState.asStateFlow()

    init {
        init()
    }

    private fun init() {
        viewModelScope.launch {
            runCatching {
                accountService.firebaseUser?.getIdToken(true)?.await()?.token?.let { idToken ->
                    Log.d("sjh", "idToken : $idToken")
                    _accountState.update { AccountState.UserAlreadySignIn }
                } ?: let {
                    _accountState.update { AccountState.UserNotSignIn }
                }
            }.onFailure {
                _accountState.update { AccountState.UserNotSignIn }
                it.printStackTrace()
            }
        }
    }

}