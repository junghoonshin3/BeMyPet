package kr.sjh.core.model.adoption.filter

import android.util.Log
import java.time.LocalDate
import java.time.format.DateTimeFormatter

val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

data class Area(
    val start: String = LocalDate.now().minusMonths(3L)
        .format(dateTimeFormatter),
    val end: String = LocalDate.now().format(dateTimeFormatter)
) {
    init {
        Log.d("sjh", "start: $start, end: $end")
    }
}