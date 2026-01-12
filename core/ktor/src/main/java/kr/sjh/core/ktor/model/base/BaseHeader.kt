package kr.sjh.core.ktor.model.base

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

/** 공통 Header */
@Serializable
@XmlSerialName("header")
data class BaseHeader(
    @XmlElement(true)
    val reqNo: String? = null,
    @XmlElement(true)
    val resultCode: String,
    @XmlElement(true)
    val resultMsg: String,
    @XmlElement(true)
    val errorMsg: String = ""
)