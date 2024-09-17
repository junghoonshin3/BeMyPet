package kr.sjh.core.ktor.repository.impl

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kr.sjh.core.ktor.model.ApiResult
import kr.sjh.core.ktor.model.Response
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
import kr.sjh.core.ktor.repository.AdoptionService
import nl.adaptivity.xmlutil.serialization.XML
import kr.sjh.core.ktor.model.Error
import javax.inject.Inject

class AdoptionServiceImpl @Inject constructor(private val client: HttpClient) : AdoptionService {

    override suspend fun getSido(sidoRequest: SidoRequest): ApiResult<SidoResponse, Error> {
        return try {
            val res = client.get("sido?") {
                parameter("serviceKey", sidoRequest.serviceKey)
                parameter("numOfRows", sidoRequest.numOfRows)
                parameter("pageNo", sidoRequest.pageNo)
                parameter("_type", sidoRequest._type)
            }
            res.successOrError<SidoResponse>()
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResult.Failure(e)
        }
    }

    override suspend fun getSigungu(sigunguRequest: SigunguRequest): ApiResult<SigunguResponse, Error> {
        return try {
            val sigungu = client.get("sigungu?") {
                parameter("serviceKey", sigunguRequest.serviceKey)
                parameter("upr_cd", sigunguRequest.upr_cd)
                parameter("_type", sigunguRequest.upr_cd)
            }
            sigungu.successOrError<SigunguResponse>()
        } catch (e: Exception) {
            ApiResult.Failure(e)
        }
    }

    override suspend fun getShelter(shelterRequest: ShelterRequest): ApiResult<ShelterResponse, Error> {
        return try {
            val shelter = client.get("shelter?") {
                parameter("serviceKey", shelterRequest.serviceKey)
                parameter("upr_cd", shelterRequest.upr_cd)
                parameter("org_cd", shelterRequest.org_cd)
                parameter("_type", shelterRequest._type)
            }
            shelter.successOrError<ShelterResponse>()
        } catch (e: Exception) {
            ApiResult.Failure(e)
        }
    }

    override suspend fun getKind(kindRequest: KindRequest): ApiResult<KindResponse, Error> {
        return try {
            val kind = client.get("kind?") {
                parameter("serviceKey", kindRequest.serviceKey)
                parameter("up_kind_cd", kindRequest.up_kind_cd)
                parameter("_type", kindRequest._type)
            }
            kind.successOrError<KindResponse>()
        } catch (e: Exception) {
            ApiResult.Failure(e)
        }
    }

    override suspend fun getAbandonmentPublic(abandonmentPublicRequest: AbandonmentPublicRequest): ApiResult<AbandonmentPublicResponse, Error> =
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
            res.successOrError<AbandonmentPublicResponse>()
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResult.Failure(e)
        }
}

private suspend inline fun <reified T : Response> HttpResponse.successOrError(): ApiResult<T, Error> {
    return if (bodyAsText().contains("OpenAPI_ServiceResponse")) {
        val error = XML.decodeFromString<Error>(bodyAsText())
        ApiResult.ServiceError(error)
    } else {
        val success = XML.decodeFromString<T>(bodyAsText())
        ApiResult.Success(success)
    }
}
