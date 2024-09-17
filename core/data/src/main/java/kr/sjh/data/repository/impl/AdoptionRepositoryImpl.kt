package kr.sjh.data.repository.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kr.sjh.core.ktor.model.ApiResult
import kr.sjh.core.ktor.model.request.AbandonmentPublicRequest
import kr.sjh.core.ktor.model.response.AbandonmentPublic
import kr.sjh.core.ktor.model.response.AbandonmentPublicResponse
import kr.sjh.core.ktor.model.response.Error
import kr.sjh.core.ktor.repository.AdoptionService
import kr.sjh.core.model.Response
import kr.sjh.core.model.adoption.Pet
import kr.sjh.data.repository.AdoptionRepository
import kr.sjh.data.toPets
import javax.inject.Inject

class AdoptionRepositoryImpl @Inject constructor(private val service: AdoptionService) :
    AdoptionRepository {
    override suspend fun getAbandonmentPublic(req: AbandonmentPublicRequest): Flow<Response<List<Pet>>> =
        flow {
            emit(Response.Loading)
            when (val res = service.getAbandonmentPublic(req)) {
                is ApiResult.Error -> {
                    val err = res.error as Error
                    val exception =
                        RuntimeException("${err.cmmMsgHeader.errMsg}:${err.cmmMsgHeader.returnAuthMsg}")
                    emit(Response.Failure(exception))
                }

                is ApiResult.Failure -> {
                    emit(Response.Failure(res.exception))
                }

                is ApiResult.Success -> {
                    val pets = (res.data as AbandonmentPublic).toPets()
                    emit(Response.Success(pets))
                }
            }
        }.flowOn(Dispatchers.IO)
}