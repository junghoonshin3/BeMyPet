package kr.sjh.core.model.adoption.filter

import androidx.compose.runtime.Immutable

@Immutable
data class Sigungu(
    val uprCd: String = "",
    val orgCd: String = "",
    val orgdownNm: String = "",
)