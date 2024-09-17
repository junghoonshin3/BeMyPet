package kr.sjh.core.ktor.model

import kr.sjh.core.ktor.model.response.Error

sealed interface ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>
    data class Error<out T>(val error: T) : ApiResult<T>
    data class Failure<out T>(val exception: Throwable) : ApiResult<T>
}