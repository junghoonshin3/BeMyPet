package kr.sjh.core.ktor.model.response

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement

// 1️⃣ 최상위 <response> 태그
@Serializable
data class SigunguItem(
    @XmlElement var uprCd: String = "", // 상위 기관 코드

    @XmlElement var orgCd: String = "", // 기관 코드

    @XmlElement var orgdownNm: String = "" // 기관명
)


