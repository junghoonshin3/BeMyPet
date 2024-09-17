package kr.sjh.core.login.screen

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.sjh.core.firebase.service.FireStoreRepository
import kr.sjh.data.repository.AuthenticationRepository
import kr.sjh.core.login.state.LoginEvent
import kr.sjh.core.login.state.NavigationState
import kr.sjh.core.model.google.AuthState
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository,
    private val fireStoreRepository: FireStoreRepository
) : ViewModel() {

    private val _navigationState = MutableSharedFlow<NavigationState>()
    val navigationState = _navigationState.asSharedFlow()


    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.OnGoogleClicked -> signInWithGoogle(context = event.context)
        }
    }

    private fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            authenticationRepository.signInUpWithGoogle(context).collect { authResult ->
                when (authResult) {
                    is AuthState.Error -> {
                        authResult.e.printStackTrace()
                    }

                    AuthState.Idle -> {}
                    AuthState.Loading -> {}
                    is AuthState.Success -> {
                        Log.d("sjh", "Success")
                        authResult.data.email?.let { email ->
                            //TODO 기존에 로그인 사람 / 첫 회원가입 하는 사람 구분
                            val isUserExist =
                                fireStoreRepository.isUserInDatabase(email).getOrThrow()
                            val navigationState = if (isUserExist) NavigationState.NavigateToMain
                            else NavigationState.NavigateToLoginRegister

                            _navigationState.emit(navigationState)
                        } ?: let {
                            _navigationState.emit(NavigationState.None)

                        }

                    }
                }
            }
        }

    }
}