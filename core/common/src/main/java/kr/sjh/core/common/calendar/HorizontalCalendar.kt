package kr.sjh.core.common.calendar

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.sjh.core.model.adoption.filter.DateRange
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth

@Composable
fun HorizontalCalendar(
    modifier: Modifier = Modifier,
    currentDate: LocalDate = LocalDate.now(),
    selectedDateRange: DateRange = DateRange(),
    yearRange: IntRange = IntRange(1970, 2100),
) {
    val initialPage = (currentDate.year - yearRange.first) * 12 + currentDate.monthValue - 1
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = {
        (yearRange.last - yearRange.first) * 12 + 1
    })
    var currentSelectedDateRange by remember { mutableStateOf(selectedDateRange) }
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var currentPage by remember { mutableIntStateOf(initialPage) }

    LaunchedEffect(pagerState.currentPage) {
        val addMonth = (pagerState.currentPage - currentPage).toLong()
        currentYearMonth = currentYearMonth.plusMonths(addMonth)
        currentPage = pagerState.currentPage
    }

    Column(modifier = modifier) {
        CalendarHeader(
            modifier = Modifier.padding(20.dp),
            text = "${currentYearMonth.year}년 ${currentYearMonth.monthValue}월"
        )
        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState,
            key = { currentPage -> currentPage },
        ) { page ->
            Log.d("sjh", "page : $page")
            val date = LocalDate.of(
                yearRange.first + page / 12,
                page % 12 + 1,
                1
            )
            CalendarContent(
                modifier = Modifier.fillMaxSize(),
                currentYear = date.year,
                currentMonth = date.monthValue,
                selectedDateRange = currentSelectedDateRange
            )
        }
    }
}


@Composable
private fun CalendarContent(
    modifier: Modifier = Modifier,
    currentYear: Int,
    currentMonth: Int,
    selectedDateRange: DateRange = DateRange(),
) {
    val currentDate = LocalDate.of(currentYear, currentMonth, 1)
    val lastDay = currentDate.lengthOfMonth()
    val cells: GridCells = GridCells.Fixed(7)
    val days = (1..lastDay).map { day ->
        LocalDate.of(currentYear, currentMonth, day)
    }
    val firstDayOfWeek = currentDate.dayOfWeek.value
    Column(modifier = modifier) {
        Log.d("sjh", "firstDayOfWeek : $firstDayOfWeek")
        DaysOfWeek(
            modifier = Modifier.fillMaxWidth(), cells = cells
        )
        LazyVerticalGrid(modifier = Modifier.fillMaxSize(), columns = cells) {
            for (i in 1..firstDayOfWeek) {
                item {
                    Box(
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            items(days) { date ->
                val today = LocalDate.now()
                val isSelected =
                    selectedDateRange.startDate <= date && date <= selectedDateRange.endDate
                val isToday = date == today

                CalendarDay(
                    modifier = Modifier.weight(1f),
                    date = date,
                    isSelected = isSelected,
                    isToday = isToday
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
    isSelected: Boolean = false,
    isToday: Boolean = false,
) {
    val color = if (isSelected) Color.Red else Color.Unspecified
    Box(
        modifier = modifier.then(
            Modifier
                .background(color)
                .padding(10.dp)
        ), contentAlignment = Alignment.Center
    ) {
        Text(text = date.dayOfMonth.toString())
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
        HorizontalCalendar(
            modifier = Modifier.fillMaxSize(),
            yearRange = IntRange(1970, 2100)
        )
    }

}

enum class DaysOfWeek(val dayOfWeek: String) {
    SUNDAY("일"), MONDAY("월"), TUESDAY("화"), WEDNESDAY("수"), THURSDAY("목"), FRIDAY("금"), SATURDAY("토")
}