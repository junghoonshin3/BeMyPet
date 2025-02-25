package kr.sjh.core.ktor.service.impl

import AbandonmentPublicResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kr.sjh.core.ktor.model.request.AbandonmentPublicRequest
import kr.sjh.core.ktor.model.request.KindRequest
import kr.sjh.core.ktor.model.request.ShelterRequest
import kr.sjh.core.ktor.model.request.SigunguRequest
import kr.sjh.core.ktor.model.response.KindResponse
import kr.sjh.core.ktor.model.response.ShelterResponse
import kr.sjh.core.ktor.model.response.SigunguResponse
import kr.sjh.core.ktor.service.AdoptionService
import org.simpleframework.xml.core.Persister
import javax.inject.Inject

class AdoptionServiceImpl @Inject constructor(private val client: HttpClient) : AdoptionService {
    private val serializer = Persister()

    override suspend fun getSigungu(sigunguRequest: SigunguRequest): SigunguResponse {
        val res = client.get("sigungu?") {
            parameter("serviceKey", sigunguRequest.serviceKey)
            parameter("upr_cd", sigunguRequest.upr_cd)
            parameter("_type", sigunguRequest._type)
        }.bodyAsText()
        return serializer.read(
            SigunguResponse::class.java, res
        )
    }

    override suspend fun getShelter(shelterRequest: ShelterRequest): ShelterResponse {
        val res = client.get("shelter?") {
            parameter("serviceKey", shelterRequest.serviceKey)
            parameter("upr_cd", shelterRequest.upr_cd)
            parameter("org_cd", shelterRequest.org_cd)
            parameter("_type", shelterRequest._type)
        }.bodyAsText()
        return serializer.read(
            ShelterResponse::class.java, res
        )

    }

    override suspend fun getKind(kindRequest: KindRequest): KindResponse {
        val res = client.get("kind?") {
            parameter("serviceKey", kindRequest.serviceKey)
            parameter("up_kind_cd", kindRequest.up_kind_cd)
            parameter("_type", kindRequest._type)
        }.bodyAsText()
        return serializer.read(
            KindResponse::class.java, res
        )
    }

    override suspend fun getAbandonmentPublic(abandonmentPublicRequest: AbandonmentPublicRequest): AbandonmentPublicResponse {
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
            parameter("_type", abandonmentPublicRequest._type)
        }.bodyAsText()
        return serializer.read(
            AbandonmentPublicResponse::class.java, res
        )
    }
}
