package kr.sjh.core.ktor.model.base

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

/** 공통 Items */
@Serializable
@XmlSerialName("items")
data class BaseItems<T>(
    @XmlSerialName("item")
    var itemList: List<T>? = null
)
