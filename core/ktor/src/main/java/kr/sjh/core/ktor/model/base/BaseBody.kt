package kr.sjh.core.ktor.model.base

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

/** 공통 Body */

@Serializable
@XmlSerialName("body")
data class BaseBody<T>(
    @XmlElement(true)
    @XmlSerialName("items")
    val items: BaseItems<T>,
    @XmlElement(true) val numOfRows: Int,
    @XmlElement(true) val pageNo: Int,
    @XmlElement(true) val totalCount: Int
)