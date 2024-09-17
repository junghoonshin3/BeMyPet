package kr.sjh.core.ktor.model.response

import kotlinx.serialization.Serializable
import kr.sjh.core.ktor.model.Response
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("response")
data class AbandonmentPublicResponse(
    @XmlElement val header: Header, @XmlElement val body: Body,
) : Response {
    @Serializable
    @XmlSerialName("header")
    data class Header(
        @XmlElement val reqNo: Long,
        @XmlElement val resultCode: String,
        @XmlElement val resultMsg: String
    )

    @Serializable
    @XmlSerialName("body")
    data class Body(
        @XmlElement val items: Items,
        @XmlElement val numOfRows: Int,
        @XmlElement val pageNo: Int,
        @XmlElement val totalCount: Int
    ) {
        @Serializable
        @XmlSerialName("items")
        data class Items(
            @XmlElement val item: List<Item>
        ) {
            @Serializable
            @XmlSerialName("item")
            data class Item(
                @XmlElement val desertionNo: String,
                @XmlElement val filename: String,
                @XmlElement val happenDt: String,
                @XmlElement val happenPlace: String,
                @XmlElement val kindCd: String,
                @XmlElement val colorCd: String,
                @XmlElement val age: String,
                @XmlElement val weight: String,
                @XmlElement val noticeNo: String,
                @XmlElement val noticeSdt: String,
                @XmlElement val noticeEdt: String,
                @XmlElement val popfile: String,
                @XmlElement val processState: String,
                @XmlElement val sexCd: String,
                @XmlElement val neuterYn: String,
                @XmlElement val specialMark: String,
                @XmlElement val careNm: String,
                @XmlElement val careTel: String,
                @XmlElement val careAddr: String,
                @XmlElement val orgNm: String,
                @XmlElement val chargeNm: String,
                @XmlElement val officetel: String
            )
        }
    }
}


