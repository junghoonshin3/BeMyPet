package kr.sjh.core.ktor.model.base

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

/** 공통 Body */
@Serializable
@Root(name = "body", strict = false)
data class BaseBody<T>(
    @XmlElement @field:Element(
        name = "items", required = false
    ) var items: BaseItems<T>? = null,

    @XmlElement @field:Element(
        name = "numOfRows", required = false
    ) var numOfRows: Int = 0,

    @XmlElement @field:Element(name = "pageNo", required = false) var pageNo: Int = 0,

    @XmlElement @field:Element(
        name = "totalCount", required = false
    ) var totalCount: Int = 0
)
