package kr.sjh.data.repository.impl

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kr.sjh.core.ktor.model.ApiResult
import kr.sjh.core.ktor.model.request.AbandonmentPublicRequest
import kr.sjh.core.ktor.model.request.SidoRequest
import kr.sjh.core.ktor.model.request.SigunguRequest
import kr.sjh.core.ktor.repository.AdoptionService
import kr.sjh.core.model.Response
import kr.sjh.core.model.adoption.Pet
import kr.sjh.core.model.adoption.filter.Sido
import kr.sjh.core.model.adoption.filter.Sigungu
import kr.sjh.data.repository.AdoptionRepository
import kr.sjh.data.toPets
import kr.sjh.data.toSidoList
import kr.sjh.data.toSigunguList
import kr.sjh.database.dao.FavouriteDao
import java.net.URLEncoder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class AdoptionRepositoryImpl @Inject constructor(
    private val service: AdoptionService,
) : AdoptionRepository {
    override suspend fun getAbandonmentPublic(req: AbandonmentPublicRequest): Flow<Response<Pair<List<Pet>, Int>>> =
        flow {
            emit(Response.Loading)
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
                    if (res.data.header.errorMsg != null) {
                        emit(Response.Failure(Exception(res.data.header.errorMsg)))
                        return@flow
                    }

                    res.data.body?.let { body ->
                        val totalCount = body.totalCount
                        val pets = body.items.toPets()
                        emit(Response.Success(Pair(pets, totalCount)))
                    }
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
                if (res.data.header.errorMsg != null) {
                    emit(Response.Failure(Exception(res.data.header.errorMsg)))
                    return@flow
                }
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
                if (res.data.header.errorMsg != null) {
                    emit(Response.Failure(Exception(res.data.header.errorMsg)))
                    return@flow
                }
                val sigunguList = res.data.toSigunguList().toMutableList().apply {
                    add(0, Sigungu())
                }.toList()
                emit(Response.Success(sigunguList))
            }
        }
    }.flowOn(Dispatchers.IO)
}