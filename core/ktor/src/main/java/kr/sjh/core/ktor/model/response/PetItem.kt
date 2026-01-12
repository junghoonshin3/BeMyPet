import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
data class PetItem(
    /** 공고번호 */
    @XmlSerialName("noticeNo")
    @XmlElement(true)
    val noticeNo: String? = null,

    /** 봉사안내 지원내용 및 신청방법 */
    @XmlSerialName("srvcTxt")
    @XmlElement(true)
    val srvcTxt: String? = null,

    /** 이미지 4 */
    @XmlSerialName("popfile4")
    @XmlElement(true)
    val popfile4: String? = null,

    /** 입양지원 종료일 */
    @XmlSerialName("sprtEDate")
    @XmlElement(true)
    val sprtEDate: String? = null,

    /** 구조번호 */
    @XmlSerialName("desertionNo")
    @XmlElement(true)
    val desertionNo: String? = null,

    /** 동물등록번호 (RFID 번호) */
    @XmlSerialName("rfidCd")
    @XmlElement(true)
    val rfidCd: String? = null,

    /** 접수일 (YYYYMMDD) */
    @XmlSerialName("happenDt")
    @XmlElement(true)
    val happenDt: String? = null,

    /** 발견 장소 */
    @XmlSerialName("happenPlace")
    @XmlElement(true)
    val happenPlace: String? = null,

    /** 품종 코드 */
    @XmlSerialName("kindCd")
    @XmlElement(true)
    val kindCd: String? = null,

    /** 색상 */
    @XmlSerialName("colorCd")
    @XmlElement(true)
    val colorCd: String? = null,

    /** 나이 */
    @XmlSerialName("age")
    @XmlElement(true)
    val age: String? = null,

    /** 체중 */
    @XmlSerialName("weight")
    @XmlElement(true)
    val weight: String? = null,

    /** 행사안내 이미지 */
    @XmlSerialName("evntImg")
    @XmlElement(true)
    val evntImg: String? = null,

    /** 수정일 (yyyy-MM-dd HH:mm:ss) */
    @XmlSerialName("updTm")
    @XmlElement(true)
    val updTm: String? = null,

    /** 처분 사유 */
    @XmlSerialName("endReason")
    @XmlElement(true)
    val endReason: String? = null,

    /** 보호소 번호 */
    @XmlSerialName("careRegNo")
    @XmlElement(true)
    val careRegNo: String? = null,

    /** 공고 시작일 (YYYYMMDD) */
    @XmlSerialName("noticeSdt")
    @XmlElement(true)
    val noticeSdt: String? = null,

    /** 공고 종료일 (YYYYMMDD) */
    @XmlSerialName("noticeEdt")
    @XmlElement(true)
    val noticeEdt: String? = null,

    /** 이미지 1 */
    @XmlSerialName("popfile1")
    @XmlElement(true)
    val popfile1: String? = null,

    /** 상태 */
    @XmlSerialName("processState")
    @XmlElement(true)
    val processState: String? = null,

    /** 성별 (M: 수컷, F: 암컷, Q: 미상) */
    @XmlSerialName("sexCd")
    @XmlElement(true)
    val sexCd: String? = null,

    /** 중성화 여부 (Y: 예, N: 아니오, U: 미상) */
    @XmlSerialName("neuterYn")
    @XmlElement(true)
    val neuterYn: String? = null,

    /** 특징 */
    @XmlSerialName("specialMark")
    @XmlElement(true)
    val specialMark: String? = null,

    /** 보호소 이름 */
    @XmlSerialName("careNm")
    @XmlElement(true)
    val careNm: String? = null,

    /** 보호소 전화번호 */
    @XmlSerialName("careTel")
    @XmlElement(true)
    val careTel: String? = null,

    /** 보호 장소 */
    @XmlSerialName("careAddr")
    @XmlElement(true)
    val careAddr: String? = null,

    /** 관할 기관 */
    @XmlSerialName("orgNm")
    @XmlElement(true)
    val orgNm: String? = null,

    /** 특징 (사회성) */
    @XmlSerialName("sfeSoci")
    @XmlElement(true)
    val sfeSoci: String? = null,

    /** 특징 (건강) */
    @XmlSerialName("sfeHealth")
    @XmlElement(true)
    val sfeHealth: String? = null,

    /** 특이 사항 */
    @XmlSerialName("etcBigo")
    @XmlElement(true)
    val etcBigo: String? = null,

    /** 품종 (풀네임) */
    @XmlSerialName("kindFullNm")
    @XmlElement(true)
    val kindFullNm: String? = null,

    /** 축종 코드 (개: 417000, 고양이: 422400, 기타: 429900) */
    @XmlSerialName("upKindCd")
    @XmlElement(true)
    val upKindCd: String? = null,

    /** 축종명 */
    @XmlSerialName("upKindNm")
    @XmlElement(true)
    val upKindNm: String? = null,

    /** 품종명 */
    @XmlSerialName("kindNm")
    @XmlElement(true)
    val kindNm: String? = null,

    /** 이미지 2 */
    @XmlSerialName("popfile2")
    @XmlElement(true)
    val popfile2: String? = null,

    /** 이미지 3 */
    @XmlSerialName("popfile3")
    @XmlElement(true)
    val popfile3: String? = null,

    /** 이미지 5 */
    @XmlSerialName("popfile5")
    @XmlElement(true)
    val popfile5: String? = null,

    /** 이미지 6 */
    @XmlSerialName("popfile6")
    @XmlElement(true)
    val popfile6: String? = null,

    /** 이미지 7 */
    @XmlSerialName("popfile7")
    @XmlElement(true)
    val popfile7: String? = null,

    /** 이미지 8 */
    @XmlSerialName("popfile8")
    @XmlElement(true)
    val popfile8: String? = null,

    /** 보호소 대표자 */
    @XmlSerialName("careOwnerNm")
    @XmlElement(true)
    val careOwnerNm: String? = null,

    /** 예방접종 체크 (광견병, 종합백신, 코로나, 호흡기 등) */
    @XmlSerialName("vaccinationChk")
    @XmlElement(true)
    val vaccinationChk: String? = null,

    /** 건강 체크 (사상충, 파보, 코로나, 홍역, 원충 등) */
    @XmlSerialName("healthChk")
    @XmlElement(true)
    val healthChk: String? = null,

    /** 입양 절차 제목 */
    @XmlSerialName("adptnTitle")
    @XmlElement(true)
    val adptnTitle: String? = null,

    /** 입양 절차 시작일 */
    @XmlSerialName("adptnSDate")
    @XmlElement(true)
    val adptnSDate: String? = null,

    /** 입양 절차 종료일 */
    @XmlSerialName("adptnEDate")
    @XmlElement(true)
    val adptnEDate: String? = null,

    /** 입양 절차 조건 및 제한 */
    @XmlSerialName("adptnConditionLimitTxt")
    @XmlElement(true)
    val adptnConditionLimitTxt: String? = null,

    /** 입양 절차 지원내용 및 신청방법 */
    @XmlSerialName("adptnTxt")
    @XmlElement(true)
    val adptnTxt: String? = null,

    /** 입양 절차 이미지 */
    @XmlSerialName("adptnImg")
    @XmlElement(true)
    val adptnImg: String? = null,

    /** 입양 지원 제목 */
    @XmlSerialName("sprtTitle")
    @XmlElement(true)
    val sprtTitle: String? = null,

    /** 입양 지원 시작일 */
    @SerialName("sprtSDate")
    @XmlElement(true)
    val sprtSDate: String? = null,

    /** 입양 지원 조건 및 제한 */
    @SerialName("sprtConditionLimitTxt")
    @XmlElement(true)
    val sprtConditionLimitTxt: String? = null,

    /** 입양 지원 내용 및 신청방법 */
    @XmlSerialName("sprtTxt")
    @XmlElement(true)
    val sprtTxt: String? = null,

    /** 입양 지원 이미지 */
    @XmlSerialName("sprtImg")
    @XmlElement(true)
    val sprtImg: String? = null,

    /** 봉사 안내 제목 */
    @XmlSerialName("srvcTitle")
    @XmlElement(true)
    val srvcTitle: String? = null,

    /** 봉사 안내 시작일 */
    @XmlSerialName("srvcSDate")
    @XmlElement(true)
    val srvcSDate: String? = null,

    /** 봉사 안내 종료일 */
    @XmlSerialName("srvcEDate")
    @XmlElement(true)
    val srvcEDate: String? = null,

    /** 봉사 안내 조건 및 제한 */
    @XmlSerialName("srvcConditionLimitTxt")
    @XmlElement(true)
    val srvcConditionLimitTxt: String? = null,

    /** 봉사 안내 이미지 */
    @XmlSerialName("srvcImg")
    @XmlElement(true)
    val srvcImg: String? = null,

    /** 행사 안내 제목 */
    @XmlSerialName("evntTitle")
    @XmlElement(true)
    val evntTitle: String? = null,

    /** 행사 안내 시작일 */
    @XmlSerialName("evntSDate")
    @XmlElement(true)
    val evntSDate: String? = null,

    /** 행사 안내 종료일 */
    @XmlSerialName("evntEDate")
    @XmlElement(true)
    val evntEDate: String? = null,

    /** 행사 안내 조건 및 제한 */
    @XmlSerialName("evntConditionLimitTxt")
    @XmlElement(true)
    val evntConditionLimitTxt: String? = null,

    /** 행사 안내 지원내용 및 신청방법 */
    @XmlSerialName("evntTxt")
    @XmlElement(true)
    val evntTxt: String? = null
)