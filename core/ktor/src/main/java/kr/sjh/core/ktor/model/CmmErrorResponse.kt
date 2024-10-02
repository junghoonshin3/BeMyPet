package kr.sjh.core.ktor.model

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("OpenAPI_ServiceResponse")
data class CmmErrorResponse(
    @XmlElement val cmmMsgHeader: CmmMsgHeader
) : Response

@Serializable
@XmlSerialName("cmmMsgHeader")
data class CmmMsgHeader(
    @XmlElement val errMsg: String = "",
    @XmlElement val returnAuthMsg: String = "",
    @XmlElement val returnReasonCode: String = ""
)