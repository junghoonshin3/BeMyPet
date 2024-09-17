package kr.sjh.core.ktor.model

sealed interface ApiResult<out T, out K> {
    data class Success<out T, out K>(val data: T) : ApiResult<T, K>
    data class ServiceError<out T, out K>(val error: K) : ApiResult<T, K>
    data class Failure<out T, out K>(val exception: Throwable) : ApiResult<T, K>
}