package kr.sjh.core.model.adoption.filter

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd(E)")

data class DateRange(
    val startDate: LocalDate? = LocalDate.now().minusDays(6),
    val endDate: LocalDate? = LocalDate.now()
) {

    private val format = DateTimeFormatter.ofPattern("yyyy.MM.dd")

    override fun toString(): String {
        return "${format.format(startDate)}~ ${format.format(endDate)}"
    }
}