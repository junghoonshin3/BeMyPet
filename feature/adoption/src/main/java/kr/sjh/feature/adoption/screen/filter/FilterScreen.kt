package kr.sjh.feature.adoption.screen.filter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.sjh.core.designsystem.components.CheckBoxButton
import kr.sjh.core.designsystem.components.DropDownMenu
import kr.sjh.core.model.FilterBottomSheetState
import kr.sjh.core.model.FilterCategory
import kr.sjh.core.model.adoption.filter.DateRange
import kr.sjh.core.model.adoption.filter.Location
import kr.sjh.core.model.adoption.filter.Option
import kr.sjh.core.model.adoption.filter.Sido
import kr.sjh.core.model.adoption.filter.Sigungu
import kr.sjh.core.model.adoption.filter.dateTimeFormatter
import kr.sjh.feature.adoption.state.AdoptionEvent
import kr.sjh.feature.adoption.state.AdoptionFilterState
import kr.sjh.feature.adoption.state.Category
import kr.sjh.feature.adoption.state.FilterOption
import kr.sjh.feature.adoption.state.NeuterOptions
import kr.sjh.feature.adoption.state.StateOptions
import kr.sjh.feature.adoption.state.UpKindOptions
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun FilterScreen(
    modifier: Modifier = Modifier,
    adoptionFilterState: AdoptionFilterState,
    onEvent: (AdoptionEvent) -> Unit
) {
    Column(modifier = modifier) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(adoptionFilterState.categories.keys.toList()) { category ->
                FilterCategoryHeader(category.categoryName)
                when (category) {
                    Category.DATE_RANGE -> {
                        DateRangeComponent(adoptionFilterState.selectedDateRange, onEvent = onEvent)
                    }

                    Category.LOCATION -> {
                        Location(
                            location = adoptionFilterState.selectedLocation,
                            sido = adoptionFilterState.sidoList,
                            sigungu = adoptionFilterState.sigunguList,
                            onEvent = onEvent
                        )
                    }

                    Category.UP_KIND, Category.STATE, Category.NEUTER -> {
                        CheckBoxList(
                            adoptionFilterState = adoptionFilterState,
                            selectedCategory = category,
                            onEvent = onEvent
                        )
                    }

                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = {
                onEvent(
                    AdoptionEvent.SelectedInit
                )
            }) {
                Text("선택 초기화")
            }
            Button(onClick = {
                onEvent(
                    AdoptionEvent.Apply
                )
                onEvent(
                    AdoptionEvent.FilterBottomSheetOpen(
                        FilterBottomSheetState.HIDE
                    )
                )
            }) {
                Text("적용하기")
            }
        }
    }
}

@Composable
private fun FilterCategoryHeader(categoryName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Text(text = categoryName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }

}

@Composable
private fun DateRangeComponent(
    dateRange: DateRange, onEvent: (AdoptionEvent) -> Unit
) {
    var dateRangePickerVisible by remember {
        mutableStateOf(false)
    }
    if (dateRangePickerVisible) {
        DateRangePickerModal(dateRange = dateRange, onDateRangeSelected = {
            onEvent(
                AdoptionEvent.SelectedDateRange(
                    it
                )
            )
        }, onDismiss = {
            dateRangePickerVisible = false
        })
    }
    Row(modifier = Modifier
        .padding(10.dp)
        .fillMaxWidth()
        .clickable {
            dateRangePickerVisible = true
        }) {
        Text(text = dateRange.startDate.format(dateTimeFormatter), fontSize = 20.sp)
        Text(text = " ~ ", fontSize = 20.sp)
        Text(text = dateRange.endDate.format(dateTimeFormatter), fontSize = 20.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Location(
    location: Location, sido: List<Sido>, sigungu: List<Sigungu>, onEvent: (AdoptionEvent) -> Unit
) {
    var expanded1 by remember {
        mutableStateOf(false)
    }
    var expanded2 by remember {
        mutableStateOf(false)
    }
    Row(modifier = Modifier.fillMaxWidth()) {
        DropDownMenu(modifier = Modifier.weight(1f),
            list = sido,
            expanded = expanded1,
            onDismissRequest = { expanded1 = false },
            onExpandedChange = {
                expanded1 = !expanded1
            },
            selectedText = {
                TextField(modifier = Modifier.menuAnchor(),
                    readOnly = true,
                    value = location.sido.orgdownNm,
                    onValueChange = {})
            }) { item ->
            Box(modifier = Modifier
                .clickable {
                    onEvent(
                        AdoptionEvent.SelectedLocation(
                            location.copy(
                                sido = item, sigungu = Sigungu()
                            ), fetchDate = true
                        )
                    )
                    expanded1 = false
                }
                .fillMaxWidth()) {
                Text(item.orgdownNm)
            }
        }
        DropDownMenu(modifier = Modifier.weight(1f),
            list = sigungu,
            expanded = expanded2,
            onDismissRequest = { expanded2 = false },
            onExpandedChange = {
                expanded2 = !expanded2
            },
            selectedText = {
                TextField(modifier = Modifier.menuAnchor(),
                    readOnly = true,
                    value = location.sigungu.orgdownNm,
                    onValueChange = {})
            }) { item ->
            Box(modifier = Modifier
                .clickable {
                    onEvent(
                        AdoptionEvent.SelectedLocation(
                            location.copy(
                                sigungu = item
                            ), fetchDate = false
                        )
                    )
                    expanded2 = false
                }
                .fillMaxWidth()) {
                Text(item.orgdownNm)
            }
        }
    }
}

@Composable
private fun CheckBoxList(
    adoptionFilterState: AdoptionFilterState,
    selectedCategory: FilterCategory,
    onEvent: (AdoptionEvent) -> Unit
) {
    val selectedOption: Option? by remember(adoptionFilterState) {
        mutableStateOf(
            when (selectedCategory) {
                Category.UP_KIND -> {
                    adoptionFilterState.selectedUpKind
                }

                Category.STATE -> {
                    adoptionFilterState.selectedState
                }

                Category.NEUTER -> {
                    adoptionFilterState.selectedNeuter
                }

                else -> {
                    null
                }
            }
        )
    }
    adoptionFilterState.categories[selectedCategory]?.let { option ->
        val list = (option as FilterOption.OneListOption).options
        list.forEach {
            CheckBoxButton(
                modifier = Modifier.padding(10.dp),
                title = it.title,
                selected = selectedOption == it
            ) {
                when (selectedCategory) {
                    Category.UP_KIND -> {
                        onEvent(
                            AdoptionEvent.SelectedUpKind(
                                it as UpKindOptions
                            )
                        )
                    }

                    Category.STATE -> onEvent(
                        AdoptionEvent.SelectedState(
                            it as StateOptions
                        )
                    )

                    Category.NEUTER -> {
                        onEvent(
                            AdoptionEvent.SelectedNeuter(
                                it as NeuterOptions
                            )
                        )

                    }

                    else -> {}
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerModal(
    dateRange: DateRange, onDateRangeSelected: (DateRange) -> Unit, onDismiss: () -> Unit
) {

    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = dateRange.startDate.atZone(ZoneId.systemDefault())
            .toInstant().toEpochMilli(),
        initialSelectedEndDateMillis = dateRange.endDate.atZone(ZoneId.systemDefault()).toInstant()
            .toEpochMilli(),
        selectableDates = PastOrPresentSelectableDates,
        yearRange = IntRange(2000, LocalDate.now().year),
        initialDisplayMode = DisplayMode.Picker
    )

    DatePickerDialog(onDismissRequest = onDismiss, confirmButton = {
        TextButton(onClick = {
            val selectedStartDate = dateRangePickerState.selectedStartDateMillis?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
            } ?: dateRange.startDate
            val selectedEndDate = dateRangePickerState.selectedEndDateMillis?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
            } ?: dateRange.endDate
            onDateRangeSelected(
                DateRange(
                    selectedStartDate, selectedEndDate
                )
            )
            onDismiss()
        }) {
            Text("확인")
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text("취소")
        }
    }) {

        DateRangePicker(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            state = dateRangePickerState,
            title = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "날짜를 선택해주세요", fontSize = 18.sp, fontWeight = FontWeight.Bold
                    )
                }
            },
            headline = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 50.dp)
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dateRangePickerState.selectedStartDateMillis?.let {
                        val startDate =
                            LocalDate.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
                        Text(text = startDate.format(dateTimeFormatter), fontSize = 15.sp)
                        Text(text = " ~ ", fontSize = 13.sp)
                    }
                    dateRangePickerState.selectedEndDateMillis?.let {
                        val endDate =
                            LocalDate.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
                        Text(text = endDate.format(dateTimeFormatter), fontSize = 15.sp)
                    }
                }
            },
            showModeToggle = false,
        )
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