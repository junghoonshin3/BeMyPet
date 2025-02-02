package kr.sjh.feature.adoption.screen.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kr.sjh.core.common.calendar.HorizontalCalendar
import java.time.LocalDate

@Composable
fun CalendarContent(
    modifier: Modifier = Modifier,
    selectedStartDate: LocalDate,
    selectedEndDate: LocalDate,
    onClose: () -> Unit,
    onConfirm: (LocalDate?, LocalDate?) -> Unit
) {
    Column(modifier = modifier) {
        HorizontalCalendar(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background),
            selectedStartDate = selectedStartDate,
            selectedEndDate = selectedEndDate,
            onClose = onClose,
            onConfirm = onConfirm
        )
    }
}