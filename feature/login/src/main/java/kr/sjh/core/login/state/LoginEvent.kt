package kr.sjh.core.login.state

import android.content.Context

sealed class LoginEvent {
    data class OnGoogleClicked(val context: Context) : LoginEvent()
}