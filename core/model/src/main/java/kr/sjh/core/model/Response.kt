package kr.sjh.core.model

sealed class Response<out T> {
    data class Success<out T>(val data: T) : Response<T>()
    data class Failure(val e: Throwable) : Response<Nothing>()
}