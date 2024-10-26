package kr.sjh.core.model.adoption

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

@Serializable
@Stable
data class Pet(
    val desertionNo: String,
    val filename: String,
    val happenDt: String,
    val happenPlace: String,
    val kindCd: String,
    val colorCd: String,
    val age: String,
    val weight: String,
    val noticeNo: String,
    val noticeSdt: String,
    val noticeEdt: String,
    val popfile: String,
    val processState: String,
    val sexCd: String,
    val neuterYn: String,
    val specialMark: String,
    val careNm: String,
    val careTel: String,
    val careAddr: String,
    val orgNm: String,
    val chargeNm: String? = null,
    val officetel: String,
    val noticeComment: String? = null
) {
    val sexCdToText: String
        get() {
            return if (sexCd == "M") "수컷" else "암컷"
        }
    val neuterYnToText: String
        get() {
            return if (neuterYn == "Y") "" else "아니요"
        }
}