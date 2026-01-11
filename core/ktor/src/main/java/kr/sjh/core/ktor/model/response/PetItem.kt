import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement

@Serializable
data class PetItem(
    @XmlElement val desertionNo: String = "",
    @XmlElement val rfidCd: String? = null,

    @XmlElement val happenDt: String = "",
    @XmlElement val happenPlace: String = "",

    @XmlElement val kindFullNm: String = "",
    @XmlElement val upKindCd: String = "",
    @XmlElement val upKindNm: String = "",
    @XmlElement val kindCd: String = "",
    @XmlElement val kindNm: String = "",

    @XmlElement val colorCd: String? = null,
    @XmlElement val age: String = "",
    @XmlElement val weight: String = "",

    @XmlElement val noticeNo: String = "",
    @XmlElement val noticeSdt: String = "",
    @XmlElement val noticeEdt: String = "",

    @XmlElement val popfile1: String? = null,
    @XmlElement val popfile2: String? = null,

    @XmlElement val processState: String = "",
    @XmlElement val sexCd: String = "",
    @XmlElement val neuterYn: String = "",

    @XmlElement val specialMark: String? = null,

    @XmlElement val careRegNo: String = "",
    @XmlElement val careNm: String = "",
    @XmlElement val careTel: String = "",
    @XmlElement val careAddr: String = "",
    @XmlElement val careOwnerNm: String? = null,

    @XmlElement val orgNm: String = "",

    // 선택 필드들 (item마다 있음/없음 섞임)
    @XmlElement val healthChk: String? = null,
    @XmlElement val vaccinationChk: String? = null,
    @XmlElement val etcBigo: String? = null,

    @XmlElement val updTm: String = ""
)