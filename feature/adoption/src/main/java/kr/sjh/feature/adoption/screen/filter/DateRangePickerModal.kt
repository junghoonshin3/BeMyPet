package kr.sjh.feature.adoption.screen.filter

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import kr.sjh.core.model.adoption.filter.DateRange
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerModal(
    dateRange: DateRange, onDateRangeSelected: (DateRange) -> Unit, onDismiss: () -> Unit
) {

    val dateRangePickerState = rememberDateRangePickerState(
//        initialSelectedStartDateMillis = dateRange.startDate
//            .toInstant().toEpochMilli()
//        ,
//        initialSelectedEndDateMillis = dateRange.endDate.atZone(ZoneId.systemDefault()).toInstant()
//            .toEpochMilli(),
//        selectableDates = PastOrPresentSelectableDates,
//        yearRange = IntRange(2000, LocalDate.now().year),
//        initialDisplayMode = DisplayMode.Picker
    )

    DatePickerDialog(onDismissRequest = onDismiss, confirmButton = {
        TextButton(colors = ButtonDefaults.textButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ), onClick = {
            val selectedStartDate = dateRangePickerState.selectedStartDateMillis?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
            } ?: dateRange.startDate
            val selectedEndDate = dateRangePickerState.selectedEndDateMillis?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
            } ?: dateRange.endDate
            onDateRangeSelected(
                DateRange(
//                    selectedStartDate, selectedEndDate
                )
            )
            onDismiss()
        }) {
            Text("확인")
        }
    }, dismissButton = {
        TextButton(
            onClick = onDismiss, colors = ButtonDefaults.textButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("취소")
        }
    }
    ) {

//        DateRangePicker(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(500.dp),
//            state = dateRangePickerState,
//            title = {
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(50.dp)
//                        .padding(5.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = "날짜를 선택해주세요", fontSize = 18.sp, fontWeight = FontWeight.Bold
//                    )
//                }
//            },
//            headline = {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .heightIn(max = 50.dp)
//                        .padding(5.dp),
//                    horizontalArrangement = Arrangement.Center,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    dateRangePickerState.selectedStartDateMillis?.let {
//                        val startDate =
//                            LocalDate.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
//                        Text(text = startDate.format(dateTimeFormatter), fontSize = 15.sp)
//                        Text(text = " ~ ", fontSize = 13.sp)
//                    }
//                    dateRangePickerState.selectedEndDateMillis?.let {
//                        val endDate =
//                            LocalDate.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
//                        Text(text = endDate.format(dateTimeFormatter), fontSize = 15.sp)
//                    }
//                }
//            },
//            showModeToggle = false,
//            colors = DatePickerDefaults.colors(
//                todayDateBorderColor = Color.Red,
//                todayContentColor = Color.Red
//            )
//        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
data object PastOrPresentSelectableDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        return utcTimeMillis <= System.currentTimeMillis()
    }

    override fun isSelectableYear(year: Int): Boolean {
        return year <= LocalDate.now().year
    }
}