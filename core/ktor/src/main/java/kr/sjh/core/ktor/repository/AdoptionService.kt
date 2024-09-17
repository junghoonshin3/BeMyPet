package kr.sjh.core.ktor.repository

import kr.sjh.core.ktor.model.ApiResult
import kr.sjh.core.ktor.model.request.AbandonmentPublicRequest
import kr.sjh.core.ktor.model.request.KindRequest
import kr.sjh.core.ktor.model.request.ShelterRequest
import kr.sjh.core.ktor.model.request.SidoRequest
import kr.sjh.core.ktor.model.request.SigunguRequest
import kr.sjh.core.ktor.model.response.AbandonmentPublicResponse
import kr.sjh.core.ktor.model.response.KindResponse
import kr.sjh.core.ktor.model.response.ShelterResponse
import kr.sjh.core.ktor.model.response.SidoResponse
import kr.sjh.core.ktor.model.response.SigunguResponse
import kr.sjh.core.ktor.model.Error

interface AdoptionService {

    //시도 조회
    suspend fun getSido(sidoRequest: SidoRequest): ApiResult<SidoResponse, Error>

    //시군구 조회
    suspend fun getSigungu(sigunguRequest: SigunguRequest): ApiResult<SigunguResponse, Error>

    //보호소 조회
    suspend fun getShelter(shelterRequest: ShelterRequest): ApiResult<ShelterResponse, Error>

    //품종 조회
    suspend fun getKind(kindRequest: KindRequest): ApiResult<KindResponse, Error>

    //유기동물 조회
    suspend fun getAbandonmentPublic(abandonmentPublicRequest: AbandonmentPublicRequest): ApiResult<AbandonmentPublicResponse, Error>
}