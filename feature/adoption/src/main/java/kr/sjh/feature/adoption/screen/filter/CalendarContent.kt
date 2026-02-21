package kr.sjh.feature.adoption.screen.filter

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kr.sjh.core.common.calendar.HorizontalCalendar
import java.time.LocalDate

@Composable
fun CalendarContent(
    modifier: Modifier = Modifier,
    selectedStartDate: LocalDate,
    selectedEndDate: LocalDate,
    close: () -> Unit,
    confirm: (LocalDate?, LocalDate?) -> Unit
) {
    Column(modifier = modifier) {
        HorizontalCalendar(
            modifier = Modifier,
            selectedStartDate = selectedStartDate,
            selectedEndDate = selectedEndDate,
            close = close,
            confirm = confirm
        )
    }
}
