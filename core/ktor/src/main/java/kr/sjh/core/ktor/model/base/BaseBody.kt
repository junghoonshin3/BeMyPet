package kr.sjh.core.ktor.model.base

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement

/** 공통 Body */
@Serializable
data class BaseBody<T>(
    @XmlElement val items: BaseItems<T>? = null,

    @XmlElement val numOfRows: Int = 0,

    @XmlElement val pageNo: Int = 0,

    @XmlElement val totalCount: Int = 0
)
