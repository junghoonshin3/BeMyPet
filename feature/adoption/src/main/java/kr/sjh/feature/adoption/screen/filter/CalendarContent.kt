package kr.sjh.feature.adoption.screen.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kr.sjh.core.common.calendar.HorizontalCalendar
import kr.sjh.core.designsystem.components.RoundedCornerButton
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
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background),
            selectedStartDate = selectedStartDate,
            selectedEndDate = selectedEndDate,
            close = close,
            confirm = confirm
        )
    }
}