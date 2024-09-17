package kr.sjh.data.repository

import kotlinx.coroutines.flow.Flow
import kr.sjh.core.ktor.model.ApiResult
import kr.sjh.core.ktor.model.request.AbandonmentPublicRequest
import kr.sjh.core.ktor.model.response.AbandonmentPublicResponse
import kr.sjh.core.model.Response
import kr.sjh.core.model.adoption.Pet

interface AdoptionRepository {
    suspend fun getAbandonmentPublic(req: AbandonmentPublicRequest): Flow<Response<List<Pet>>>
}