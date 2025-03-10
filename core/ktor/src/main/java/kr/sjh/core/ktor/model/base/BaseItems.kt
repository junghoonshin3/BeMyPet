package kr.sjh.core.ktor.model.base

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

/** 공통 Items */
@Serializable
@XmlSerialName("items")
data class BaseItems<T>(
    @XmlSerialName("item")
    var itemList: List<T>? = null
)
