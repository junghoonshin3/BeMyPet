package kr.sjh.core.model.adoption.filter

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd(E)")

data class DateRange(
    val startDate: LocalDateTime = LocalDateTime.now().minusDays(6),
    val endDate: LocalDateTime = LocalDateTime.now()
) {
    private val format = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    fun isInitSameDate(): Boolean {
        return startDate.format(dateTimeFormatter) == LocalDateTime.now().minusDays(6)
            .format(dateTimeFormatter) && endDate.format(dateTimeFormatter) == LocalDateTime.now()
            .format(dateTimeFormatter)
    }

    override fun toString(): String {
        return "${format.format(startDate)}~ ${format.format(endDate)}"
    }
}