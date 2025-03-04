import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement

@Serializable
data class PetItem(
    @XmlElement val desertionNo: String = "",
    @XmlElement val filename: String = "",
    @XmlElement val happenDt: String = "",
    @XmlElement val happenPlace: String = "",
    @XmlElement val kindCd: String = "",
    @XmlElement val colorCd: String? = null,
    @XmlElement val age: String = "",
    @XmlElement val weight: String = "",
    @XmlElement val noticeNo: String = "",
    @XmlElement val noticeSdt: String = "",
    @XmlElement val noticeEdt: String = "",
    @XmlElement val popfile: String = "",
    @XmlElement val processState: String = "",
    @XmlElement val sexCd: String = "",
    @XmlElement val neuterYn: String = "",
    @XmlElement val specialMark: String = "",
    @XmlElement val careNm: String = "",
    @XmlElement val careTel: String = "",
    @XmlElement val careAddr: String = "",
    @XmlElement val orgNm: String = "",
    @XmlElement val chargeNm: String? = null,
    @XmlElement val officetel: String = "",
    @XmlElement var noticeComment: String? = null
)
