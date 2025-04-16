package kr.sjh.core.common.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.sjh.core.designsystem.components.RoundedCornerButton
import java.time.LocalDate

@Composable
fun HorizontalCalendar(
    modifier: Modifier = Modifier,
    selectedStartDate: LocalDate,
    selectedEndDate: LocalDate,
    yearRange: IntRange = IntRange(1970, LocalDate.now().year),
    close: () -> Unit,
    confirm: (LocalDate?, LocalDate?) -> Unit
) {
    val currentDate = LocalDate.now()
    val pageCount = (yearRange.last - yearRange.first) * 12 + currentDate.month.value
    val initialPage =
        (selectedEndDate.year - yearRange.first) * 12 + (selectedEndDate.month.value - 1)
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = {
        pageCount
    })

    var startDate: LocalDate? by remember { mutableStateOf(selectedStartDate) }

    var endDate: LocalDate? by remember { mutableStateOf(selectedEndDate) }

    val currentYear by remember(pagerState.currentPage) {
        derivedStateOf {
            yearRange.first + pagerState.currentPage / 12
        }
    }

    val currentMonth by remember(pagerState.currentPage) {
        derivedStateOf {
            pagerState.currentPage % 12 + 1
        }
    }

    Column(modifier = modifier) {
        CalendarHeader(
            modifier = Modifier.padding(20.dp), text = "${currentYear}년 ${currentMonth}월"
        )
        HorizontalPager(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(1.dp),
            state = pagerState,
            key = { currentPage -> currentPage },
        ) { page ->
            val date = LocalDate.of(
                yearRange.first + page / 12, page % 12 + 1, 1
            )

            CalendarContent(modifier = Modifier.fillMaxWidth(),
                currentYear = date.year,
                currentMonth = date.monthValue,
                startDate = startDate,
                endDate = endDate,
                onSelectedDate = { selectedDate ->
                    val (newStartDate, newEndDate) = handleDateSelection(
                        selectedDate, startDate, endDate
                    )
                    startDate = newStartDate
                    endDate = newEndDate
                })
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            RoundedCornerButton(
                modifier = Modifier
                    .width(50.dp)
                    .height(30.dp)
                    .padding(10.dp),
                title = "닫기",
                onClick = close
            )
            RoundedCornerButton(modifier = Modifier
                .width(50.dp)
                .height(30.dp)
                .padding(10.dp),
                title = "확인",
                onClick = { confirm(startDate, endDate) })
        }

    }


}

private fun handleDateSelection(
    selectedDate: LocalDate, currentStartDate: LocalDate?, currentEndDate: LocalDate?
): Pair<LocalDate?, LocalDate?> {
    return when {
        // 시작 날짜가 없으면 시작 날짜로 설정
        currentStartDate == null -> selectedDate to null
        // 끝 날짜가 없으면 끝 날짜로 설정 (시작 날짜보다 이전이면 교체)
        currentEndDate == null -> {
            if (selectedDate.isBefore(currentStartDate)) {
                selectedDate to currentStartDate
            } else {
                currentStartDate to selectedDate
            }
        }
        // 시작 날짜와 끝 날짜가 모두 있으면 새로운 시작 날짜로 초기화
        else -> selectedDate to null
    }
}


@Composable
private fun CalendarContent(
    modifier: Modifier = Modifier,
    currentYear: Int,
    currentMonth: Int,
    onSelectedDate: (LocalDate) -> Unit,
    startDate: LocalDate?,
    endDate: LocalDate?,
) {
    val currentDate = LocalDate.of(currentYear, currentMonth, 1)
    val lastDay = currentDate.lengthOfMonth()
    val cells: GridCells = GridCells.Fixed(7)
    val days = (1..lastDay).map { day ->
        LocalDate.of(currentYear, currentMonth, day)
    }
    val firstDayOfWeek = currentDate.dayOfWeek.value

    val today = LocalDate.now()

    Column(modifier = modifier.heightIn(min = 350.dp)) {
        DaysOfWeek(
            modifier = Modifier.fillMaxWidth(), cells = cells
        )
        LazyVerticalGrid(columns = cells) {
            for (i in 1..firstDayOfWeek) {
                item {
                    Box(
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            items(days, key = { it.toString() }) { date ->
                val isStartDateSelected =
                    remember(date, startDate) { startDate != null && date == startDate }
                val isEndDateSelected =
                    remember(date, endDate) { endDate != null && date == endDate }
                val isDateInRange = remember(date, startDate, endDate) {
                    startDate != null && endDate != null && date in startDate..endDate
                }

                val isSelectable = remember(date, today) { date <= today }

                CalendarDay(
                    modifier = Modifier.aspectRatio(1f),
                    date = date,
                    isSelectable = isSelectable,
                    isStartDateSelected = isStartDateSelected,
                    isEndDateSelected = isEndDateSelected,
                    isDateInRange = isDateInRange,
                    onSelectedDate = onSelectedDate
                )
            }
        }
    }
}

@Composable
private fun DaysOfWeek(modifier: Modifier = Modifier, cells: GridCells) {
    val daysOfWeek = DaysOfWeek.entries
    LazyVerticalGrid(columns = cells, modifier = modifier) {
        items(daysOfWeek) { dayOfWeek ->
            DayOfWeek(
                modifier = Modifier.padding(10.dp), dayOfWeek = dayOfWeek
            )
        }
    }
}

@Composable
private fun DayOfWeek(modifier: Modifier = Modifier, dayOfWeek: DaysOfWeek) {
    val color = when (dayOfWeek) {
        DaysOfWeek.SUNDAY -> Color.Red
        DaysOfWeek.SATURDAY -> Color.Blue
        else -> Color.Unspecified
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            dayOfWeek.dayOfWeek, style = MaterialTheme.typography.bodySmall.copy(color = color)
        )
    }
}

@Composable
private fun CalendarDay(
    modifier: Modifier = Modifier,
    date: LocalDate,
    isSelectable: Boolean,
    isStartDateSelected: Boolean,
    isEndDateSelected: Boolean,
    isDateInRange: Boolean,
    onSelectedDate: (LocalDate) -> Unit
) {
    Box(modifier = modifier
        .drawBehind {
            if (isStartDateSelected) {
                // 동그란 반원 (왼쪽)
                drawArc(
                    color = Color.Red,
                    startAngle = 90f,
                    sweepAngle = 180f,
                    useCenter = true,
                )

                // 오른쪽 직사각형 이어붙이기
                if (isEndDateSelected || isDateInRange) {
                    drawRect(
                        color = Color.Red,
                        topLeft = Offset(size.width / 2, 0f),
                        size = Size(size.width / 2, size.height)
                    )
                } else {
                    // 시작만 선택한 경우에는 완전한 원형
                    drawCircle(color = Color.Red)
                }
                return@drawBehind
            }

            if (isEndDateSelected) {
                // 동그란 반원 (오른쪽)
                drawArc(
                    color = Color.Red,
                    startAngle = 270f,
                    sweepAngle = 180f,
                    useCenter = true,
                )

                // 왼쪽 직사각형 이어붙이기
                drawRect(
                    color = Color.Red,
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width / 2, size.height)
                )
                return@drawBehind
            }

            if (isDateInRange) {
                // 중간 날짜는 그냥 직사각형으로 연결
                drawRect(color = Color.Red)
                return@drawBehind
            }
        }
        .clickable(enabled = isSelectable) {
            onSelectedDate(date)
        }
        .padding(10.dp), contentAlignment = Alignment.Center) {
        Text(
            color = if (isSelectable) MaterialTheme.colorScheme.onPrimary else Color.Gray.copy(0.5f),
            text = date.dayOfMonth.toString()
        )
    }
}

@Composable
private fun CalendarHeader(modifier: Modifier = Modifier, text: String) {
    Box(modifier = modifier) {
        Text(
            text = text, style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
@Preview
fun HorizontalCalendarPreview() {
    MaterialTheme {
        HorizontalCalendar(modifier = Modifier.fillMaxSize(),
            yearRange = IntRange(1970, 2100),
            selectedStartDate = LocalDate.now(),
            selectedEndDate = LocalDate.now(),
            confirm = { a, b -> },
            close = {})
    }

}

enum class DaysOfWeek(val dayOfWeek: String) {
    SUNDAY("일"), MONDAY("월"), TUESDAY("화"), WEDNESDAY("수"), THURSDAY("목"), FRIDAY("금"), SATURDAY(
        "토"
    )
}