package kr.sjh.core.login.state

sealed class NavigationState {
    data object None : NavigationState()
    data object NavigateToMain : NavigationState()
    data object NavigateToLoginRegister : NavigationState()
}