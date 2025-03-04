package kr.sjh.data.repository.impl

import android.util.Log
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kr.sjh.core.ktor.model.request.PetRequest
import kr.sjh.core.ktor.model.request.SigunguRequest
import kr.sjh.core.ktor.service.PetService
import kr.sjh.core.model.adoption.Pet
import kr.sjh.core.model.adoption.filter.Sido
import kr.sjh.core.model.adoption.filter.Sigungu
import kr.sjh.data.repository.AdoptionRepository
import kr.sjh.data.toEntities
import kr.sjh.data.toPets
import kr.sjh.database.dao.LocationDao
import kr.sjh.database.entity.SidoEntity
import kr.sjh.database.entity.SigunguEntity
import java.util.concurrent.CancellationException
import javax.inject.Inject

class AdoptionRepositoryImpl @Inject constructor(
    private val service: PetService, private val dao: LocationDao
) : AdoptionRepository {

    override fun getPets(req: PetRequest): Flow<List<Pet>> = flow {
        val res = service.getPets(req)
        val pets = res.body?.items?.itemList?.toPets() ?: emptyList()
        emit(pets)
    }


    override suspend fun insertSidoList() {
        try {
            withContext(Dispatchers.IO) {
                if (dao.existSido()) return@withContext
                dao.insertAllSido(
                    listOf(
                        SidoEntity(orgdownNm = "서울특별시", orgCd = "6110000"),
                        SidoEntity(orgdownNm = "부산광역시", orgCd = "6260000"),
                        SidoEntity(orgdownNm = "대구광역시", orgCd = "6270000"),
                        SidoEntity(orgdownNm = "인천광역시", orgCd = "6280000"),
                        SidoEntity(orgdownNm = "광주광역시", orgCd = "6290000"),
                        SidoEntity(orgdownNm = "세종특별자치시", orgCd = "5690000"),
                        SidoEntity(orgdownNm = "대전광역시", orgCd = "6300000"),
                        SidoEntity(orgdownNm = "울산광역시", orgCd = "6310000"),
                        SidoEntity(orgdownNm = "경기도", orgCd = "6410000"),
                        SidoEntity(orgdownNm = "강원특별자치도", orgCd = "6530000"),
                        SidoEntity(orgdownNm = "충청북도", orgCd = "6430000"),
                        SidoEntity(orgdownNm = "충청남도", orgCd = "6440000"),
                        SidoEntity(orgdownNm = "전북특별자치도", orgCd = "6540000"),
                        SidoEntity(orgdownNm = "전라남도", orgCd = "6460000"),
                        SidoEntity(orgdownNm = "경상북도", orgCd = "6470000"),
                        SidoEntity(orgdownNm = "경상남도", orgCd = "6480000"),
                        SidoEntity(orgdownNm = "제주특별자치도", orgCd = "6500000")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun insertSigunguList(list: List<Sigungu>) {
        try {
            val sigunguListEntity =
                list.map { SigunguEntity(it.orgCd, it.orgdownNm, it.orgdownNm) }.toMutableList()
            sigunguListEntity.add(0, SigunguEntity())
            dao.insertAllSigungu(sigunguListEntity)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getSidoList(): Flow<List<Sido>> =
        dao.getSidoList().map { it.map { Sido(orgCd = it.orgCd, orgdownNm = it.orgdownNm) } }.map {
            val list = it.toMutableList()
            list.add(0, Sido(orgCd = "", orgdownNm = "전국"))
            list
        }


    override fun getSigunguList(sido: Sido): Flow<List<Sigungu>> = flow {
        if (sido.orgCd.isBlank()) {
            emit(emptyList())
            return@flow
        }
        if (!dao.existSigunguList(sido.orgCd)) {
            val res = service.getSigungu(
                SigunguRequest(
                    upr_cd = sido.orgCd
                )
            )
            if (res.header.resultCode == "00") {
                val sigunguList = res.body?.items?.itemList ?: emptyList()
                dao.insertAllSigungu(sigunguList.toEntities())
            }
        }
        val sigunguList = dao.getSigunguList(sido.orgCd).map {
            Sigungu(it.uprCd, it.orgCd, it.orgdownNm)
        }
        emit(sigunguList)
    }
}