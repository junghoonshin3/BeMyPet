package kr.sjh.core.ktor.service.impl

import PetItem
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kr.sjh.core.ktor.model.base.BaseResponse
import kr.sjh.core.ktor.model.request.PetRequest
import kr.sjh.core.ktor.model.request.SigunguRequest
import kr.sjh.core.ktor.model.response.SigunguItem
import kr.sjh.core.ktor.service.PetService
import javax.inject.Inject

class PetServiceImpl @Inject constructor(private val client: HttpClient) : PetService {

    override suspend fun getSigungu(sigunguRequest: SigunguRequest): BaseResponse<SigunguItem> {
        val res = client.get("sigungu?") {
            parameter("serviceKey", sigunguRequest.serviceKey)
            parameter("upr_cd", sigunguRequest.upr_cd)
            parameter("_type", sigunguRequest._type)
        }.body<BaseResponse<SigunguItem>>()
        return res
    }

    override suspend fun getPets(petReq: PetRequest): BaseResponse<PetItem> {
        val res = client.get("abandonmentPublic?") {
            parameter("bgnde", petReq.bgnde)
            parameter("endde", petReq.endde)
            parameter("upkind", petReq.upkind?.ifBlank { null })
            parameter("upr_cd", petReq.upr_cd?.ifBlank { null })
            parameter("org_cd", petReq.org_cd?.ifBlank { null })
            parameter("neuter_yn", petReq.neuter_yn?.ifBlank { null })
            parameter("pageNo", petReq.pageNo)
            parameter("numOfRows", petReq.numOfRows)
        }.body<BaseResponse<PetItem>>()
        return res
    }
}
