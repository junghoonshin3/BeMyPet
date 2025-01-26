package kr.sjh.core.model.adoption.filter

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd(E)")

data class DateRange(
    val startDate: LocalDate = LocalDate.now().minusDays(6),
    val endDate: LocalDate = LocalDate.now()
) {
    private val format = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    fun isInitSameDate(): Boolean {
        return startDate.format(dateTimeFormatter) == LocalDate.now().minusDays(6)
            .format(dateTimeFormatter) && endDate.format(dateTimeFormatter) == LocalDate.now()
            .format(dateTimeFormatter)
    }

    override fun toString(): String {
        return "${format.format(startDate)}~ ${format.format(endDate)}"
    }
}