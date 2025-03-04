package kr.sjh.data.repository

import kotlinx.coroutines.flow.Flow
import kr.sjh.core.ktor.model.request.PetRequest
import kr.sjh.core.model.Response
import kr.sjh.core.model.adoption.Pet
import kr.sjh.core.model.adoption.filter.Sido
import kr.sjh.core.model.adoption.filter.Sigungu

interface AdoptionRepository {

    suspend fun insertSidoList()

    suspend fun insertSigunguList(list: List<Sigungu>)

    fun getSidoList(): Flow<List<Sido>>

    fun getSigunguList(sido: Sido): Flow<List<Sigungu>>

    fun getPets(req: PetRequest): Flow<List<Pet>>
}