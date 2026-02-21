package kr.sjh.core.common.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kr.sjh.core.designsystem.theme.RoundedCorner12
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val CalendarSummaryFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

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
    val coroutineScope = rememberCoroutineScope()

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
    val canConfirm = startDate != null && endDate != null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CalendarHeader(
            modifier = Modifier.fillMaxWidth(),
            text = "${currentYear}년 ${currentMonth}월",
            onPrevious = {
                if (pagerState.currentPage > 0) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                }
            },
            onNext = {
                if (pagerState.currentPage < pageCount - 1) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            }
        )
        SelectedRangeSummary(startDate = startDate, endDate = endDate)
        HorizontalPager(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(0.dp),
            state = pagerState,
            key = { currentPage -> currentPage },
        ) { page ->
            val date = LocalDate.of(
                yearRange.first + page / 12, page % 12 + 1, 1
            )

            CalendarContent(
                modifier = Modifier.fillMaxWidth(),
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
                }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f).height(48.dp),
                onClick = close,
                shape = RoundedCorner12,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = "닫기",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Button(
                modifier = Modifier.weight(1f).height(48.dp),
                onClick = { confirm(startDate, endDate) },
                enabled = canConfirm,
                shape = RoundedCorner12,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            ) {
                Text(
                    text = "적용",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (canConfirm) {
                        MaterialTheme.colorScheme.onSecondary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    }
                )
            }
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
    val firstDayOffset = currentDate.dayOfWeek.value % 7
    val dayCells = buildList<LocalDate?> {
        repeat(firstDayOffset) { add(null) }
        addAll(days)
        val remainder = size % 7
        if (remainder != 0) {
            repeat(7 - remainder) { add(null) }
        }
    }

    val today = LocalDate.now()

    Column(
        modifier = modifier.heightIn(min = 320.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        DaysOfWeek(modifier = Modifier.fillMaxWidth())
        LazyVerticalGrid(
            columns = cells,
            userScrollEnabled = false
        ) {
            itemsIndexed(dayCells, key = { index, date ->
                date?.toString() ?: "empty-$index"
            }) { _, date ->
                if (date == null) {
                    Spacer(modifier = Modifier.aspectRatio(1f))
                    return@itemsIndexed
                }

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
private fun DaysOfWeek(modifier: Modifier = Modifier) {
    val daysOfWeek = DaysOfWeek.entries
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        daysOfWeek.forEach { dayOfWeek ->
            DayOfWeek(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 6.dp),
                dayOfWeek = dayOfWeek
            )
        }
    }
}

@Composable
private fun DayOfWeek(modifier: Modifier = Modifier, dayOfWeek: DaysOfWeek) {
    val color = when (dayOfWeek) {
        DaysOfWeek.SUNDAY -> MaterialTheme.colorScheme.error
        DaysOfWeek.SATURDAY -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = dayOfWeek.dayOfWeek,
            style = MaterialTheme.typography.bodySmall,
            color = color
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
    val isSelected = isStartDateSelected || isEndDateSelected
    val showRangeBackground = isDateInRange && !isSelected
    val rangeColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)
    val dayCircleColor = if (isSelected) {
        MaterialTheme.colorScheme.secondary
    } else {
        Color.Transparent
    }
    val dayTextColor = when {
        isSelected -> MaterialTheme.colorScheme.onSecondary
        !isSelectable -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
        date.dayOfWeek.value % 7 == 0 -> MaterialTheme.colorScheme.error
        date.dayOfWeek.value % 7 == 6 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .padding(2.dp)
            .clickable(enabled = isSelectable) { onSelectedDate(date) },
        contentAlignment = Alignment.Center
    ) {
        if (showRangeBackground) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(vertical = 8.dp)
                    .background(rangeColor)
            )
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(dayCircleColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = dayTextColor
            )
        }
    }
}

@Composable
private fun CalendarHeader(
    modifier: Modifier = Modifier,
    text: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            modifier = Modifier.height(40.dp),
            onClick = onPrevious,
            shape = RoundedCorner12,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text(
                text = "이전",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedButton(
            modifier = Modifier.height(40.dp),
            onClick = onNext,
            shape = RoundedCorner12,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text(
                text = "다음",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SelectedRangeSummary(
    startDate: LocalDate?,
    endDate: LocalDate?,
) {
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCorner12
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = startDate?.format(CalendarSummaryFormatter) ?: "시작일",
                style = MaterialTheme.typography.bodySmall,
                color = if (startDate == null) placeholderColor else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = "  -  ",
            style = MaterialTheme.typography.bodySmall,
            color = placeholderColor
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = endDate?.format(CalendarSummaryFormatter) ?: "종료일",
                style = MaterialTheme.typography.bodySmall,
                color = if (endDate == null) placeholderColor else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

enum class DaysOfWeek(val dayOfWeek: String) {
    SUNDAY("일"), MONDAY("월"), TUESDAY("화"), WEDNESDAY("수"), THURSDAY("목"), FRIDAY("금"), SATURDAY(
        "토"
    )
}
