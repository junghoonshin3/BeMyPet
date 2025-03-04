package kr.sjh.core.ktor.service

import PetItem
import kr.sjh.core.ktor.model.base.BaseResponse
import kr.sjh.core.ktor.model.request.PetRequest
import kr.sjh.core.ktor.model.request.SigunguRequest
import kr.sjh.core.ktor.model.response.SigunguItem

interface PetService {

    //시군구 조회
    suspend fun getSigungu(sigunguRequest: SigunguRequest): BaseResponse<SigunguItem>

    //유기동물 조회
    suspend fun getPets(petReq: PetRequest): BaseResponse<PetItem>
}