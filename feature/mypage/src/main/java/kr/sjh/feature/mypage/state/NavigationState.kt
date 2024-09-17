package kr.sjh.feature.mypage.state

sealed class NavigationState {
    data object None : NavigationState()
    data object NavigationToLogin : NavigationState()
}