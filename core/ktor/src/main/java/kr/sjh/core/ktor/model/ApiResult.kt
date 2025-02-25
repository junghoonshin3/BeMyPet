package kr.sjh.core.ktor.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface ApiResult<out T, out K> {
    data class Success<out T>(val data: T) : ApiResult<T, Nothing>
    data class ServiceError<out K>(val error: K) : ApiResult<Nothing, K>
    data class Failure(val exception: Throwable) : ApiResult<Nothing, Nothing>
}

sealed class ApiResult2<out T> {
    data class Success<T>(val data: T) : ApiResult2<T>()
    data class Error(val e: Exception) : ApiResult2<Nothing>()
    object Loading : ApiResult2<Nothing>()
}

suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResult2<T> {
    return try {
        val result = withContext(Dispatchers.IO) { apiCall() }
        ApiResult2.Success(result)
    } catch (e: Exception) {
        ApiResult2.Error(e)
    }
}