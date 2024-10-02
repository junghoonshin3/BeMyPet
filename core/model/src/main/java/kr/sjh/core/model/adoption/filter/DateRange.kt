package kr.sjh.core.model.adoption.filter

import java.time.LocalDate
import java.time.format.DateTimeFormatter

val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

data class DateRange(
    val startDate: String = LocalDate.now().minusMonths(3L).format(dateTimeFormatter),
    val endDate: String = LocalDate.now().format(dateTimeFormatter)
) {
    fun isValidation(): Boolean = startDate <= endDate
}