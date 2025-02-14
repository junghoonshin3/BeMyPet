package kr.sjh.data.repository

import kotlinx.coroutines.flow.Flow
import kr.sjh.core.ktor.model.request.AbandonmentPublicRequest
import kr.sjh.core.ktor.model.request.SidoRequest
import kr.sjh.core.ktor.model.request.SigunguRequest
import kr.sjh.core.model.Response
import kr.sjh.core.model.adoption.Pet
import kr.sjh.core.model.adoption.filter.Sido
import kr.sjh.core.model.adoption.filter.Sigungu

interface AdoptionRepository {
    suspend fun getAbandonmentPublic(req: AbandonmentPublicRequest): Flow<Response<Pair<List<Pet>, Int>>>
    fun getSido(req: SidoRequest): Flow<Response<List<Sido>>>
    fun getSigungu(req: SigunguRequest): Flow<Response<List<Sigungu>>>
}