package kr.sjh.core.ktor.model.response

import kotlinx.serialization.Serializable
import kr.sjh.core.ktor.model.Header
import kr.sjh.core.ktor.model.Response
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("response")
data class SigunguResponse(
    @XmlElement val header: Header, @XmlElement val body: Body? = null
) : Response {

    @Serializable
    @XmlSerialName("body")
    data class Body(
        @XmlElement val items: Items,
    ) {
        @Serializable
        @XmlSerialName("items")
        data class Items(
            @XmlElement val item: List<Item>
        ) {

            @Serializable
            @XmlSerialName("item")
            data class Item(
                @XmlElement val uprCd: String,
                @XmlElement val orgCd: String,
                @XmlElement val orgdownNm: String
            )
        }
    }
}
