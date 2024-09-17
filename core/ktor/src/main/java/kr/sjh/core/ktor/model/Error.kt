package kr.sjh.core.ktor.model

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("OpenAPI_ServiceResponse")
data class Error(
    @XmlElement(true) val cmmMsgHeader: CmmMsgHeader
)

@Serializable
@XmlSerialName("cmmMsgHeader")
data class CmmMsgHeader(
    @XmlElement(true) val errMsg: String = "",
    @XmlElement(true) val returnAuthMsg: String = "",
    @XmlElement(true) val returnReasonCode: String = ""
)