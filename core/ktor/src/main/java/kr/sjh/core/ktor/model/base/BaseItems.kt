package kr.sjh.core.ktor.model.base

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

/** 공통 Items */
@Serializable
@XmlSerialName("item")
data class BaseItems<T>(
    @XmlElement(true)
    @XmlSerialName("item") val item: List<T>
)
