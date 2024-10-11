package kr.sjh.core.common.snackbar

import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object SnackBarManager {
    private val messages = MutableStateFlow<Pair<String, SnackbarDuration?>?>(null)
    val snackBarMessages get() = messages.asStateFlow()

    fun showMessage(message: String, duration: SnackbarDuration? = null) {
        messages.update { Pair(message, duration) }
    }

    fun clean() {
        messages.update { null }
    }
}