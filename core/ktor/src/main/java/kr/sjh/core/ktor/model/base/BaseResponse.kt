package kr.sjh.core.ktor.model.base

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

/** 공통 Response */
@Serializable
@XmlSerialName("response")
data class BaseResponse<T>(
    @XmlElement(true)
    @XmlSerialName("header") val header: BaseHeader,
    @XmlElement(true)
    @XmlSerialName("body") val body: BaseBody<T>
)