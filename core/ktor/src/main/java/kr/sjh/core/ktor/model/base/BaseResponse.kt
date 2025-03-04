package kr.sjh.core.ktor.model.base

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

/** 공통 Response */
@Serializable
data class BaseResponse<T>(
    @XmlSerialName("header") var header: BaseHeader = BaseHeader(),
    @XmlSerialName("body") var body: BaseBody<T>? = null
)