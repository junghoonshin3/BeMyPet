package kr.sjh.core.model.adoption.filter

import androidx.compose.runtime.Immutable

@Immutable
data class Sido(
    val orgCd: String = "",
    val orgdownNm: String = "전국",
)