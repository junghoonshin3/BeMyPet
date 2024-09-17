package kr.sjh.core.ktor.model.request

import kotlinx.serialization.Serializable
import kr.sjh.core.ktor.BuildConfig
import kr.sjh.core.ktor.model.JSON
import kr.sjh.core.ktor.model.XML

@Serializable
data class AbandonmentPublicRequest(
    val serviceKey: String = BuildConfig.SERVER_KEY,
    val bgnde: String? = null, // 유기 시작일 (YYYYMMDD)
    val endde: String? = null, // 유기 종료일 (YYYYMMDD)
    val upkind: String? = null, // 축종 코드 (optional)
    val kind: String? = null, // 품종 코드 (optional)
    val upr_cd: String? = null, // 시도 코드 (optional)
    val org_cd: String? = null, // 시군구 코드 (optional)
    val care_reg_no: String? = null, // 보호소 번호 (optional)
    val state: String? = null, // 상태 (optional)
    val neuter_yn: String? = null, // 중성화 여부 (optional)
    val pageNo: Int = 1, // 페이지 번호 (기본값 1)
    val numOfRows: Int = 10, // 페이지당 개수 (기본값 10)
    val _type: String = JSON // 응답 형식 (기본값 xml)
)