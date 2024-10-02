package kr.sjh.core.ktor.model

sealed interface ApiResult<out T, out K> {
    data class Success<out T>(val data: T) : ApiResult<T, Nothing>
    data class ServiceError<out K>(val error: K) : ApiResult<Nothing, K>
    data class Failure(val exception: Throwable) : ApiResult<Nothing, Nothing>
}