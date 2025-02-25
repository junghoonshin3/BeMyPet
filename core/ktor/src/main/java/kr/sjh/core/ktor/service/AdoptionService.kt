package kr.sjh.core.ktor.service

import AbandonmentPublicResponse
import kr.sjh.core.ktor.model.request.AbandonmentPublicRequest
import kr.sjh.core.ktor.model.request.KindRequest
import kr.sjh.core.ktor.model.request.ShelterRequest
import kr.sjh.core.ktor.model.request.SigunguRequest
import kr.sjh.core.ktor.model.response.KindResponse
import kr.sjh.core.ktor.model.response.ShelterResponse
import kr.sjh.core.ktor.model.response.SigunguResponse

interface AdoptionService {

    //시군구 조회
    suspend fun getSigungu(sigunguRequest: SigunguRequest): SigunguResponse

    //보호소 조회
    suspend fun getShelter(shelterRequest: ShelterRequest): ShelterResponse

    //품종 조회
    suspend fun getKind(kindRequest: KindRequest): KindResponse

    //유기동물 조회
    suspend fun getAbandonmentPublic(abandonmentPublicRequest: AbandonmentPublicRequest): AbandonmentPublicResponse
}