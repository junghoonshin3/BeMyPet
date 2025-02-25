package kr.sjh.feature.adoption.screen.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.sjh.core.model.adoption.filter.Sido
import kr.sjh.data.repository.AdoptionRepository
import kr.sjh.feature.adoption.state.FilterEvent
import kr.sjh.feature.adoption.state.FilterUiState
import kr.sjh.feature.adoption.state.SideEffect
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class FilterViewModel @Inject constructor(private val adoptionRepository: AdoptionRepository) :
    ViewModel() {

    private val _filterUiState = MutableStateFlow(FilterUiState())
    val filterUiState = _filterUiState.asStateFlow()

    private val _sideEffect = Channel<SideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    private var fetchJob: Job? = null

    init {
        insertSido()
        getSido()
    }

    fun onEvent(event: FilterEvent) {
        when (event) {
            is FilterEvent.SelectedCategory -> {
                _filterUiState.update {
                    it.copy(
                        selectedCategory = event.category
                    )
                }
                showBottomSheet()
            }

            is FilterEvent.SelectedNeuter -> {
                _filterUiState.update {
                    it.copy(
                        selectedNeuter = event.neuter
                    )
                }
            }

            is FilterEvent.SelectedSido -> {
                _filterUiState.update {
                    it.copy(
                        selectedSido = event.sido
                    )
                }
                getSigungu(event.sido)
            }

            is FilterEvent.SelectedSigungu -> {
                _filterUiState.update {
                    it.copy(
                        selectedSigungu = event.sigungu
                    )
                }
            }

            is FilterEvent.SelectedUpKind -> {
                _filterUiState.update {
                    it.copy(
                        selectedUpKind = event.upKind
                    )
                }
            }

            FilterEvent.Reset -> {
                _filterUiState.update {
                    it.copy(selectedCategory = null, categoryList = it.categoryList.map {
                        it.reset()
                        it
                    })
                }
            }

            FilterEvent.CloseBottomSheet -> {
                hideBottomSheet()
            }

            FilterEvent.OpenBottomSheet -> {
                showBottomSheet()
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
        }.catch { e ->
            if (e is CancellationException) throw e  // 코루틴 취소 예외는 다시 던지기

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

    private fun hideBottomSheet() {
        viewModelScope.launch {
            _sideEffect.send(SideEffect.HideBottomSheet)
        }
    }
}