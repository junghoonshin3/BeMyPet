package kr.sjh.core.ktor.model

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName


@Serializable
@XmlSerialName("response")
data class ErrorResponse(
    @XmlElement val header: Header
) : Response

@Serializable
@XmlSerialName("header")
data class Header(
    @XmlElement val reqNo: Long,
    @XmlElement val resultCode: String,
    @XmlElement val resultMsg: String,
    @XmlElement val errorMsg: String? = null,
)