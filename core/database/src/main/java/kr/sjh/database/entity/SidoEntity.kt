package kr.sjh.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("sido")
data class SidoEntity(
    @PrimaryKey val orgCd: String,
    val orgdownNm: String,
)