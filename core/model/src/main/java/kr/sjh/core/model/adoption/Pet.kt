package kr.sjh.core.model.adoption

import android.os.Parcelable
import androidx.compose.runtime.Stable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Stable
@Serializable
@Parcelize
data class Pet(
    val desertionNo: String = "",
    val rfidCd: String? = null,

    val happenDt: String = "",
    val happenPlace: String = "",

    val kindFullNm: String = "",
    val upKindCd: String = "",
    val upKindNm: String = "",
    val kindCd: String = "",
    val kindNm: String = "",

    val colorCd: String? = null,
    val age: String = "",
    val weight: String = "",

     val noticeNo: String = "",
     val noticeSdt: String = "",
     val noticeEdt: String = "",

     val popfile1: String? = null,
     val popfile2: String? = null,

     val processState: String = "",
     val sexCd: String = "",
     val neuterYn: String = "",

     val specialMark: String? = null,

     val careRegNo: String = "",
     val careNm: String = "",
     val careTel: String = "",
     val careAddr: String = "",
     val careOwnerNm: String? = null,
     val orgNm: String = "",
     val healthChk: String? = null,
     val vaccinationChk: String? = null,
     val etcBigo: String? = null,

     val updTm: String = ""
) : Parcelable {
    val sexCdToText: String
        get() {
            return if (sexCd == "M") "수컷" else "암컷"
        }
    val neuterYnToText: String
        get() {
            return if (neuterYn == "Y") "예" else "아니요"
        }
    val isNotice: Boolean
        get() {
            val noticeDateFormat = DateTimeFormatter.ofPattern("yyyyMMdd")
            val today = LocalDate.now().format(noticeDateFormat)
            return today in noticeSdt..noticeEdt
        }
}

