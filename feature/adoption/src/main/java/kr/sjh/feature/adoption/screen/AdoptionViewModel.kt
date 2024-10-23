package kr.sjh.feature.adoption.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.sjh.core.common.snackbar.SnackBarManager
import kr.sjh.core.ktor.model.request.AbandonmentPublicRequest
import kr.sjh.core.ktor.model.request.SidoRequest
import kr.sjh.core.ktor.model.request.SigunguRequest
import kr.sjh.core.model.FilterBottomSheetState
import kr.sjh.core.model.Response
import kr.sjh.core.model.adoption.filter.DateRange
import kr.sjh.core.model.adoption.filter.Location
import kr.sjh.data.repository.AdoptionRepository
import kr.sjh.feature.adoption.state.AdoptionEvent
import kr.sjh.feature.adoption.state.AdoptionFilterState
import kr.sjh.feature.adoption.state.AdoptionUiState
import kr.sjh.feature.adoption.state.Category
import kr.sjh.feature.adoption.state.NeuterOptions
import kr.sjh.feature.adoption.state.StateOptions
import kr.sjh.feature.adoption.state.UpKindOptions
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AdoptionViewModel @Inject constructor(
    private val adoptionRepository: AdoptionRepository
) : ViewModel() {

    private val _adoptionUiState = MutableStateFlow(AdoptionUiState())
    val adoptionUiState = _adoptionUiState.asStateFlow()

    private val _adoptionFilterState = MutableStateFlow(AdoptionFilterState())
    val adoptionFilterState = _adoptionFilterState.asStateFlow()

    init {
        getAbandonmentPublic(_adoptionFilterState.value.toAbandonmentPublicRequest())
    }

    fun onEvent(event: AdoptionEvent) {
        when (event) {
            AdoptionEvent.Refresh -> {
                _adoptionFilterState.update { it.copy(pageNo = 1) }
                getAbandonmentPublic(
                    _adoptionFilterState.value.toAbandonmentPublicRequest()
                )
            }

            is AdoptionEvent.LoadMore -> {
                _adoptionFilterState.update { it.copy(pageNo = it.pageNo.plus(1)) }
                getLoadMore(
                    _adoptionFilterState.value.toAbandonmentPublicRequest()
                )
            }

            is AdoptionEvent.SelectedCategory -> {
                _adoptionFilterState.update {
                    it.copy(
                        filterBottomSheetState = FilterBottomSheetState.SHOW
                    )
                }
            }

            is AdoptionEvent.FilterBottomSheetOpen -> {
                _adoptionFilterState.update {
                    it.copy(
                        filterBottomSheetState = event.bottomSheetState,
                    )
                }
            }

            is AdoptionEvent.SelectedDateRange -> {
                _adoptionFilterState.update {
                    it.copy(
                        selectedDateRange = event.dateRange,
                    )
                }
            }

            AdoptionEvent.Apply -> {
                _adoptionFilterState.update {
                    it.copy(selectedCategory = it.selectedCategory.toMutableList().apply {
                        if (it.selectedUpKind != UpKindOptions.ALL) {
                            add(Category.UP_KIND)
                        } else {
                            remove(Category.UP_KIND)
                        }

                        if (it.selectedState != StateOptions.ALL) {
                            add(Category.STATE)
                        } else {
                            remove(Category.STATE)
                        }

                        if (it.selectedNeuter != NeuterOptions.ALL) {
                            add(Category.NEUTER)
                        } else {
                            remove(Category.NEUTER)
                        }

                        if (it.selectedLocation.sido.orgCd != null || it.selectedLocation.sigungu.orgCd != null) {
                            add(Category.LOCATION)
                        } else {
                            remove(Category.LOCATION)
                        }

                        if (!it.selectedDateRange.isInitSameDate()) {
                            add(Category.DATE_RANGE)
                        } else {
                            remove(Category.DATE_RANGE)
                        }
                    }, pageNo = 1)
                }
                getAbandonmentPublic(
                    _adoptionFilterState.value.toAbandonmentPublicRequest()
                )
            }

            AdoptionEvent.SelectedInit -> {
                //초기화
                _adoptionFilterState.update {
                    it.copy(
                        selectedUpKind = UpKindOptions.ALL,
                        selectedState = StateOptions.ALL,
                        selectedNeuter = NeuterOptions.ALL,
                        selectedLocation = Location(),
                        selectedDateRange = DateRange(),
                    )
                }
            }

            is AdoptionEvent.SelectedLocation -> {
                _adoptionFilterState.update {
                    if (event.fetchDate) {
                        getSigungu(SigunguRequest(upr_cd = event.location.sido.orgCd))
                    }

                    it.copy(
                        selectedLocation = event.location
                    )
                }

            }

            is AdoptionEvent.SelectedNeuter -> {
                _adoptionFilterState.update {
                    it.copy(
                        selectedNeuter = event.neuter
                    )
                }
            }

            is AdoptionEvent.SelectedState -> {
                _adoptionFilterState.update {
                    it.copy(
                        selectedState = event.state
                    )
                }
            }

            is AdoptionEvent.SelectedUpKind -> {
                _adoptionFilterState.update {
                    it.copy(
                        selectedUpKind = event.upKind
                    )
                }
            }

            is AdoptionEvent.SetLastScrollIndex -> {
                _adoptionUiState.update {
                    it.copy(
                        lastScrollIndex = event.index
                    )
                }
            }
        }
    }


    private fun getAbandonmentPublic(request: AbandonmentPublicRequest = AbandonmentPublicRequest()) {
        viewModelScope.launch {
            adoptionRepository.getAbandonmentPublic(request).collect { result ->
                when (result) {
                    is Response.Failure -> {
                        result.e.message?.let { msg ->
                            SnackBarManager.showMessage(msg)
                        }
                        _adoptionUiState.update {
                            //TODO 에러가 난 경우 팝업 or 바텀 시트 다이얼로그 생성
                            it.copy(
                                isRefreshing = false
                            )
                        }
                    }

                    Response.Loading -> {
                        Log.d("sjh", "Loading")
                        _adoptionUiState.update {
                            it.copy(
                                isRefreshing = true
                            )
                        }
                    }

                    is Response.Success -> {
                        _adoptionUiState.update {
                            it.copy(
                                isRefreshing = false,
                                pets = result.data.first,
                                totalCount = result.data.second
                            )
                        }
                    }
                }

            }
        }
    }

    private fun getLoadMore(request: AbandonmentPublicRequest = AbandonmentPublicRequest()) {
        viewModelScope.launch {
            adoptionRepository.getAbandonmentPublic(request).collect { result ->
                when (result) {
                    is Response.Failure -> {
                        result.e.message?.let { msg ->
                            SnackBarManager.showMessage(msg)
                        }
                        _adoptionUiState.update {
                            //TODO 에러가 난 경우 팝업
                            it.copy(
                                isMore = false
                            )
                        }
                    }

                    Response.Loading -> {
                        Log.d("sjh", "getLoadMore")
                        _adoptionUiState.update {
                            it.copy(
                                isMore = true
                            )
                        }
                    }

                    is Response.Success -> {
                        _adoptionUiState.update {
                            it.copy(
                                isMore = false,
                                pets = it.pets.plus(result.data.first),
                                totalCount = result.data.second
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getSigungu(req: SigunguRequest) {
        viewModelScope.launch {
            adoptionRepository.getSigungu(req).collect { result ->
                when (result) {
                    is Response.Failure -> {
                        result.e.message?.let { msg ->
                            SnackBarManager.showMessage(msg)
                        }
                        _adoptionFilterState.update {
                            it.copy(
                                isSigunguLoading = false
                            )
                        }
                    }

                    Response.Loading -> {
                        _adoptionFilterState.update {
                            it.copy(
                                isSigunguLoading = true
                            )
                        }
                    }

                    is Response.Success -> {
                        Log.d("sjh", "list : ${result.data}")
                        _adoptionFilterState.update {
                            it.copy(
                                sigunguList = result.data, isSigunguLoading = false
                            )
                        }
                    }
                }
            }
        }
    }
}
