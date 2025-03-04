package kr.sjh.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("sigungu")
data class SigunguEntity(
    @PrimaryKey
    val orgCd: String = "",
    val uprCd: String = "",
    val orgdownNm: String = "전국",
)