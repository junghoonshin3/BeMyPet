package kr.sjh.core.model

sealed class Response<out T> {
    data object Loading : Response<Nothing>()
    data class Success<out T>(val data: T) : Response<T>()
    data class Failure(val e: Exceptions) : Response<Nothing>()
}

sealed interface LoadingState {
    data object Idle : LoadingState
    data object Loading : LoadingState
}