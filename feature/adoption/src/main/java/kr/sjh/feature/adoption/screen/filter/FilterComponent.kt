import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.composables.core.ModalBottomSheet
import com.composables.core.ModalBottomSheetState
import com.composables.core.Scrim
import com.composables.core.Sheet
import kr.sjh.feature.adoption.screen.filter.CalendarContent
import kr.sjh.feature.adoption.screen.filter.CategoryType
import kr.sjh.feature.adoption.screen.filter.LocationContent
import kr.sjh.feature.adoption.screen.filter.NeuterContent
import kr.sjh.feature.adoption.screen.filter.UpKindContent
import kr.sjh.feature.adoption.state.FilterEvent
import kr.sjh.feature.adoption.state.FilterUiState
import kr.sjh.feature.adoption.state.dateRangeFormater
import java.time.LocalDate

@Composable
fun FilterComponent(
    modifier: Modifier = Modifier,
    filterUiState: FilterUiState,
    sheetState: ModalBottomSheetState,
    onFilterEvent: (FilterEvent) -> Unit,
) {
    if (filterUiState.selectedCategory == null) return

    ModalBottomSheet(state = sheetState, onDismiss = {
        onFilterEvent(FilterEvent.CloseBottomSheet)
    }) {
        Scrim()
        Sheet(
            modifier = modifier, enabled = false
        ) {
            when (filterUiState.selectedCategory.type) {
                CategoryType.DATE_RANGE -> {
                    val startDate =
                        LocalDate.parse(filterUiState.selectedStartDate, dateRangeFormater)
                    val endDate = LocalDate.parse(filterUiState.selectedEndDate, dateRangeFormater)
                    CalendarContent(selectedStartDate = startDate,
                        selectedEndDate = endDate,
                        close = {
                            onFilterEvent(FilterEvent.CloseBottomSheet)
                        },
                        confirm = { startDate, endDate ->
                            val start = startDate?.format(dateRangeFormater)
                            val end = endDate?.format(dateRangeFormater)
                            if (!start.isNullOrBlank() && !end.isNullOrBlank()) {
                                onFilterEvent(FilterEvent.ConfirmDateRange(start, end))
                            }
                        })
                }

                CategoryType.NEUTER -> {
                    NeuterContent(title = filterUiState.selectedCategory.type.title,
                        selectedNeuter = filterUiState.selectedNeuter,
                        confirm = { neuter ->
                            onFilterEvent(FilterEvent.ConfirmNeuter(neuter))
                        },
                        close = {
                            onFilterEvent(FilterEvent.CloseBottomSheet)
                        })
                }

                CategoryType.LOCATION -> {
                    LocationContent(
                        isLoading = filterUiState.isLoading,
                        title = filterUiState.selectedCategory.type.title,
                        sidoList = filterUiState.sidoList,
                        sigunguList = filterUiState.sigunguList,
                        selectedSido = filterUiState.selectedSido,
                        selectedSigungu = filterUiState.selectedSigungu,
                        onFilterEvent = onFilterEvent
                    )
                }

                CategoryType.UP_KIND -> {
                    UpKindContent(title = filterUiState.selectedCategory.type.title,
                        selectedUpKind = filterUiState.selectedUpKind,
                        confirm = { upKind ->
                            onFilterEvent(FilterEvent.ConfirmUpKind(upKind))
                        },
                        close = {
                            onFilterEvent(FilterEvent.CloseBottomSheet)
                        })
                }

            }
        }
    }
}
