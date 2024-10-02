package kr.sjh.core.ktor.model.response

import kotlinx.serialization.Serializable
import kr.sjh.core.ktor.model.Header
import kr.sjh.core.ktor.model.Response
import kr.sjh.core.ktor.model.response.SigunguResponse.Body
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("response")
data class ShelterResponse(
    @XmlElement val header: Header,
    @XmlElement val body: SigunguResponse.Body? = null
) : Response {

    @Serializable
    @XmlSerialName("body")
    data class Body(
        @XmlElement val items: Items
    ) {
        @Serializable
        @XmlSerialName("items")
        data class Items(
            @XmlElement val item: List<Item>
        ) {
            @Serializable
            @XmlSerialName("item")
            data class Item(
                @XmlElement val careRegNo: String,
                @XmlElement val careNm: String
            )
        }
    }
}