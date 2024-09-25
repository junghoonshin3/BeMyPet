package kr.sjh.data.repository.impl

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kr.sjh.core.ktor.model.ApiResult
import kr.sjh.core.ktor.model.request.AbandonmentPublicRequest
import kr.sjh.core.ktor.model.request.KindRequest
import kr.sjh.core.ktor.model.request.SidoRequest
import kr.sjh.core.ktor.model.request.SigunguRequest
import kr.sjh.core.ktor.repository.AdoptionService
import kr.sjh.core.model.Response
import kr.sjh.core.model.adoption.Pet
import kr.sjh.core.model.adoption.filter.Kind
import kr.sjh.core.model.adoption.filter.Sido
import kr.sjh.core.model.adoption.filter.Sigungu
import kr.sjh.data.repository.AdoptionRepository
import kr.sjh.data.toKindList
import kr.sjh.data.toPets
import kr.sjh.data.toSidoList
import kr.sjh.data.toSigunguList
import javax.inject.Inject

class AdoptionRepositoryImpl @Inject constructor(private val service: AdoptionService) :
    AdoptionRepository {
    override suspend fun getAbandonmentPublic(req: AbandonmentPublicRequest): Flow<Response<List<Pet>>> =
        flow {
            emit(Response.Loading)
            Log.d("sjh", "req: $req")
            when (val res = service.getAbandonmentPublic(req)) {
                is ApiResult.ServiceError -> {
                    val err = res.error
                    val exception =
                        RuntimeException("${err.cmmMsgHeader.errMsg}:${err.cmmMsgHeader.returnAuthMsg}")
                    emit(Response.Failure(exception))
                }

                is ApiResult.Failure -> {
                    emit(Response.Failure(res.exception))
                }

                is ApiResult.Success -> {
                    val pets = res.data.toPets()
                    emit(Response.Success(pets))
                }
            }
        }.flowOn(Dispatchers.IO)

    override fun getSido(req: SidoRequest): Flow<Response<List<Sido>>> = flow {
        emit(Response.Loading)
        when (val res = service.getSido(req)) {
            is ApiResult.ServiceError -> {
                val err = res.error
                val exception =
                    RuntimeException("${err.cmmMsgHeader.errMsg}:${err.cmmMsgHeader.returnAuthMsg}")
                emit(Response.Failure(exception))
            }

            is ApiResult.Failure -> {
                emit(Response.Failure(res.exception))
            }

            is ApiResult.Success -> {
                val sidoList = res.data.toSidoList().toMutableList().apply {
                    add(0, Sido())
                }.toList()
                emit(Response.Success(sidoList))
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun getSigungu(req: SigunguRequest): Flow<Response<List<Sigungu>>> = flow {
        emit(Response.Loading)
        when (val res = service.getSigungu(req)) {
            is ApiResult.ServiceError -> {
                val err = res.error
                val exception =
                    RuntimeException("${err.cmmMsgHeader.errMsg}:${err.cmmMsgHeader.returnAuthMsg}")
                emit(Response.Failure(exception))
            }

            is ApiResult.Failure -> {
                emit(Response.Failure(res.exception))
            }

            is ApiResult.Success -> {
                val sigunguList = res.data.toSigunguList().toMutableList().apply {
                    add(0, Sigungu())
                }.toList()
                emit(Response.Success(sigunguList))
            }
        }
    }.flowOn(Dispatchers.IO)
}