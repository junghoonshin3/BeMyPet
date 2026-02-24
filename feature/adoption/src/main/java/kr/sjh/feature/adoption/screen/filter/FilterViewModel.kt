package kr.sjh.feature.adoption.screen.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.sjh.core.common.snackbar.SnackBarManager
import kr.sjh.core.model.adoption.filter.Sido
import kr.sjh.core.model.adoption.filter.Sigungu
import kr.sjh.data.repository.AdoptionRepository
import kr.sjh.feature.adoption.state.Category
import kr.sjh.feature.adoption.state.FilterEvent
import kr.sjh.feature.adoption.state.FilterUiState
import kr.sjh.feature.adoption.state.Neuter
import kr.sjh.feature.adoption.state.SideEffect
import kr.sjh.feature.adoption.state.UpKind
import kr.sjh.feature.adoption.state.dateRangeFormater
import java.time.LocalDate
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class FilterViewModel @Inject constructor(private val adoptionRepository: AdoptionRepository) :
    ViewModel() {

    val snackBarManager = SnackBarManager

    private val _filterUiState = MutableStateFlow(FilterUiState())
    val filterUiState = _filterUiState.asStateFlow()

    private val _sideEffect = Channel<SideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    private var fetchJob: Job? = null

    init {
        insertSido()
        getSido()
        fetchPets()
    }

    fun onEvent(event: FilterEvent) {
        when (event) {
            is FilterEvent.SelectedCategory -> {
                selectedCategory(event.category)
                showBottomSheet()
            }

            FilterEvent.Reset -> {
                clearCategories()
            }

            FilterEvent.CloseBottomSheet -> {
                requestHideBottomSheet()
            }

            FilterEvent.BottomSheetDismissed -> {
                onBottomSheetDismissed()
            }

            FilterEvent.OpenBottomSheet -> {
                showBottomSheet()
            }

            is FilterEvent.ConfirmLocation -> {
                confirmLocation(event.sido, event.sigungu)
                requestHideBottomSheet()
            }

            is FilterEvent.FetchSigungu -> {
                getSigungu(event.sido)
            }

            is FilterEvent.ConfirmNeuter -> {
                confirmNeuter(event.neuter)
                requestHideBottomSheet()
            }

            is FilterEvent.ConfirmUpKind -> {
                confirmUpKind(event.upkind)
                requestHideBottomSheet()
            }

            is FilterEvent.ConfirmDateRange -> {
                confirmDateRange(event.startDate, event.endDate)
                requestHideBottomSheet()
            }
        }
    }

    private fun insertSido() {
        viewModelScope.launch {
            adoptionRepository.insertSidoList()
        }
    }

    private fun getSido() {
        adoptionRepository.getSidoList().onEach { sidoList ->
            _filterUiState.update {
                it.copy(
                    sidoList = sidoList, errorMsg = ""
                )
            }
        }.catch { e ->
            e.printStackTrace()
            _filterUiState.update {
                it.copy(
                    sidoList = emptyList(), errorMsg = e.message.toString()
                )
            }
        }.flowOn(Dispatchers.IO).launchIn(viewModelScope)
    }

    private fun getSigungu(sido: Sido) {
        fetchJob?.cancel()
        fetchJob = adoptionRepository.getSigunguList(sido).onStart {
            _filterUiState.update {
                it.copy(isLoading = true, sigunguList = emptyList(), errorMsg = "")
            }
        }.retryWhen { cause, attempt ->
            delay(500)
            attempt < 2
        }.catch { e ->
            snackBarManager.showMessage("서버 상태가 원활하지 않아 데이터를 불러오지 못했습니다.")
            //그외 다른 Exception 들은 Ui상태를 업데이트
            _filterUiState.update {
                it.copy(
                    isLoading = false,
                    sigunguList = emptyList(),
                    errorMsg = e.message ?: "알 수 없는 오류"
                )
            }
        }.onEach { sigunguList ->
            _filterUiState.update {
                it.copy(isLoading = false, sigunguList = sigunguList, errorMsg = "")
            }
        }.flowOn(Dispatchers.IO).launchIn(viewModelScope)
    }

    private fun showBottomSheet() {
        viewModelScope.launch {
            _sideEffect.send(SideEffect.ShowBottomSheet)
        }
    }

    private fun requestHideBottomSheet() {
        if (_filterUiState.value.selectedCategory == null) return
        viewModelScope.launch {
            _sideEffect.send(SideEffect.HideBottomSheet)
        }
    }

    private fun onBottomSheetDismissed() {
        if (_filterUiState.value.selectedCategory == null) return
        fetchJob?.cancel()
        clearSigungu()
        _filterUiState.update {
            it.copy(selectedCategory = null)
        }
    }

    private fun selectedCategory(category: Category) {
        _filterUiState.update {
            it.copy(
                selectedCategory = category
            )
        }
    }

    private fun confirmLocation(sido: Sido, sigungu: Sigungu) {
        _filterUiState.update {
            val text = if (sido.orgCd.isBlank()) sido.orgdownNm
            else "${sido.orgdownNm} ${sigungu.orgdownNm}"
            val base = if (sido.orgCd.isBlank()) {
                it.copy(selectedSido = Sido(), selectedSigungu = Sigungu())
            } else {
                it.copy(selectedSido = sido, selectedSigungu = sigungu)
            }
            base.withUpdatedCategory { cat ->
                cat.copy(isSelected = true, selectedText = text)
            }
        }
        fetchPets()
    }

    private fun confirmNeuter(neuter: Neuter) {
        _filterUiState.update {
            it.copy(selectedNeuter = neuter).withUpdatedCategory { cat ->
                cat.copy(isSelected = true, selectedText = neuter.title)
            }
        }
        fetchPets()
    }

    private fun confirmUpKind(upKind: UpKind) {
        _filterUiState.update {
            it.copy(selectedUpKind = upKind).withUpdatedCategory { cat ->
                cat.copy(isSelected = true, selectedText = upKind.title)
            }
        }
        fetchPets()
    }

    private fun clearSigungu() {
        // 전국인 경우 시군구 리스트 초기화
        if (_filterUiState.value.selectedSido.orgCd == "") {
            _filterUiState.update {
                it.copy(
                    sigunguList = emptyList()
                )
            }
        }
    }

    private fun clearCategories() {
        _filterUiState.update {
            it.copy(
                selectedStartDate = LocalDate.now().minusDays(7).format(dateRangeFormater),
                selectedEndDate = LocalDate.now().format(dateRangeFormater),
                categoryList = it.categoryList.map { cat -> Category(type = cat.type) },
                selectedCategory = null,
                selectedSido = Sido(),
                selectedSigungu = Sigungu(),
                selectedNeuter = Neuter.ALL,
                selectedUpKind = UpKind.ALL
            )
        }
        fetchPets()
    }

    private fun confirmDateRange(start: String, end: String) {
        _filterUiState.update {
            it.copy(
                selectedStartDate = start, selectedEndDate = end
            ).withUpdatedCategory { cat ->
                cat.copy(isSelected = true, selectedText = "$start ~ $end")
            }
        }
        fetchPets()
    }

    private fun FilterUiState.withUpdatedCategory(
        transform: (Category) -> Category
    ): FilterUiState {
        val updated = selectedCategory?.let(transform)
        return copy(
            selectedCategory = updated,
            categoryList = if (updated != null) {
                categoryList.map { if (it.type == updated.type) updated else it }
            } else categoryList
        )
    }

    private fun fetchPets() {
        val req = _filterUiState.value.toPetRequest()
        viewModelScope.launch {
            _sideEffect.send(SideEffect.FetchPets(req))
        }
    }
}
