package kr.sjh.core.ktor.model.base

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement

/** 공통 Header */
@Serializable
data class BaseHeader(
    @XmlElement var reqNo: String = "",

    @XmlElement var resultCode: String = "",

    @XmlElement var resultMsg: String = "",

    @XmlElement(false) var errorMsg: String? = null
)