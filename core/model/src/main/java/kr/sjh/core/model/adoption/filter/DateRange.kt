package kr.sjh.core.model.adoption.filter

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd(E)")

data class DateRange(
    val startDate: LocalDateTime = LocalDateTime.now().minusDays(7),
    val endDate: LocalDateTime = LocalDateTime.now()
) {
    fun isInitSameDate(): Boolean {
        return startDate.format(dateTimeFormatter) == LocalDateTime.now().minusDays(7)
            .format(dateTimeFormatter) && endDate.format(dateTimeFormatter) == LocalDateTime.now()
            .format(dateTimeFormatter)
    }
}