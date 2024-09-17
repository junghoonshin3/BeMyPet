package kr.sjh.core.ktor.repository.impl

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kr.sjh.core.ktor.model.ApiResult
import kr.sjh.core.ktor.model.request.AbandonmentPublicRequest
import kr.sjh.core.ktor.model.request.KindRequest
import kr.sjh.core.ktor.model.request.ShelterRequest
import kr.sjh.core.ktor.model.request.SidoRequest
import kr.sjh.core.ktor.model.request.SigunguRequest
import kr.sjh.core.ktor.model.response.AbandonmentPublic
import kr.sjh.core.ktor.model.response.AbandonmentPublicResponse
import kr.sjh.core.ktor.model.response.Error
import kr.sjh.core.ktor.model.response.KindResponse
import kr.sjh.core.ktor.model.response.ShelterResponse
import kr.sjh.core.ktor.model.response.SidoResponse
import kr.sjh.core.ktor.model.response.SigunguResponse
import kr.sjh.core.ktor.repository.AdoptionService
import nl.adaptivity.xmlutil.serialization.XML
import javax.inject.Inject

class AdoptionServiceImpl @Inject constructor(private val client: HttpClient) : AdoptionService {

    override suspend fun getSido(sidoRequest: SidoRequest): ApiResult<SidoResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getSigungu(sigunguRequest: SigunguRequest): ApiResult<SigunguResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getShelter(shelterRequest: ShelterRequest): ApiResult<ShelterResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getKind(kindRequest: KindRequest): ApiResult<KindResponse> {
        TODO("Not yet implemented")
    }

//    override suspend fun getSido(sidoRequest: SidoRequest) = ApiResult<SidoResponse>> {
//        val sido = client.get("sido?") {
//            parameter("serviceKey", sidoRequest.serviceKey)
//            parameter("numOfRows", sidoRequest.numOfRows)
//            parameter("pageNo", sidoRequest.pageNo)
//            parameter("_type", sidoRequest._type)
//        }.body<SidoResponse>()
//        Log.d("success", "sido >>>>>>>>>>>>>>> $sido")
//        emit(Response.Success(sido))
//    }.catch { e ->
//        emit(Response.Failure(e))
//    }.flowOn(Dispatchers.IO)
//
//    override suspend fun getSigungu(sigunguRequest: SigunguRequest) =
//        flow<Response<SigunguResponse>> {
//            val sigungu = client.get("sigungu?") {
//                parameter("serviceKey", sigunguRequest.serviceKey)
//                parameter("upr_cd", sigunguRequest.upr_cd)
//                parameter("_type", sigunguRequest.upr_cd)
//            }.body<SigunguResponse>()
//            Log.d("success", "sigungu >>>>>>>>>>>>>>> $sigungu")
//            emit(Response.Success(sigungu))
//        }.catch { e ->
//            emit(Response.Failure(e))
//        }.flowOn(Dispatchers.IO)
//
//    override suspend fun getShelter(shelterRequest: ShelterRequest) =
//        flow<Response<ShelterResponse>> {
//            val shelter = client.get("shelter?") {
//                parameter("serviceKey", shelterRequest.serviceKey)
//                parameter("upr_cd", shelterRequest.upr_cd)
//                parameter("org_cd", shelterRequest.org_cd)
//                parameter("_type", shelterRequest._type)
//            }.body<ShelterResponse>()
//            Log.d("success", "sigungu >>>>>>>>>>>>>>> $shelter")
//            emit(Response.Success(shelter))
//        }.catch { e ->
//            emit(Response.Failure(e))
//        }.flowOn(Dispatchers.IO)
//
//
//    override suspend fun getKind(kindRequest: KindRequest) = flow<Response<KindResponse>> {
//        val shelter = client.get("kind?") {
//            parameter("serviceKey", kindRequest.serviceKey)
//            parameter("up_kind_cd", kindRequest.up_kind_cd)
//            parameter("_type", kindRequest._type)
//        }.body<KindResponse>()
//        Log.d("success", "sigungu >>>>>>>>>>>>>>> $shelter")
//        emit(Response.Success(shelter))
//    }.catch { e ->
//        emit(Response.Failure(e))
//    }.flowOn(Dispatchers.IO)

    override suspend fun getAbandonmentPublic(abandonmentPublicRequest: AbandonmentPublicRequest): ApiResult<AbandonmentPublicResponse> =
        try {
            val res = client.get("abandonmentPublic?") {
                parameter("serviceKey", abandonmentPublicRequest.serviceKey)
                parameter("bgnde", abandonmentPublicRequest.bgnde)
                parameter("endde", abandonmentPublicRequest.endde)
                parameter("upkind", abandonmentPublicRequest.upkind)
                parameter("kind", abandonmentPublicRequest.kind)
                parameter("upr_cd", abandonmentPublicRequest.upr_cd)
                parameter("org_cd", abandonmentPublicRequest.org_cd)
                parameter("care_reg_no", abandonmentPublicRequest.care_reg_no)
                parameter("state", abandonmentPublicRequest.state)
                parameter("neuter_yn", abandonmentPublicRequest.neuter_yn)
                parameter("pageNo", abandonmentPublicRequest.pageNo)
                parameter("numOfRows", abandonmentPublicRequest.numOfRows)
                parameter("_type", "xml")
            }
            if (res.bodyAsText().contains("OpenAPI_ServiceResponse")) {
                val error = XML.decodeFromString<Error>(res.bodyAsText())
                ApiResult.Error(error)
            } else {
                val success = XML.decodeFromString<AbandonmentPublic>(res.bodyAsText())
                ApiResult.Success(success)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResult.Failure(e)
        }
}

//suspend fun <T> HttpResponse.successOrError(): ApiResult<T> {
//    return if (bodyAsText().contains("OpenAPI_ServiceResponse")) {
//        val error = XML.decodeFromString<Error>(bodyAsText())
//        ApiResul()
//    } else {
//        val success = XML.decodeFromString<AbandonmentPublic>(bodyAsText())
//        ApiResult.Success(success)
//    }
//}
