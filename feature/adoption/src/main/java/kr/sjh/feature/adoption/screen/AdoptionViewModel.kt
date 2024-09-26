package kr.sjh.feature.adoption.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.sjh.core.ktor.model.request.AbandonmentPublicRequest
import kr.sjh.core.ktor.model.request.SidoRequest
import kr.sjh.core.ktor.model.request.SigunguRequest
import kr.sjh.core.model.FilterBottomSheetState
import kr.sjh.core.model.Response
import kr.sjh.core.model.adoption.filter.Sigungu
import kr.sjh.data.repository.AdoptionRepository
import kr.sjh.feature.adoption.state.AdoptionEvent
import kr.sjh.feature.adoption.state.AdoptionFilterOptionState
import kr.sjh.feature.adoption.state.AdoptionFilterState
import kr.sjh.feature.adoption.state.AdoptionUiState
import javax.inject.Inject

@HiltViewModel
class AdoptionViewModel @Inject constructor(
    private val adoptionRepository: AdoptionRepository
) : ViewModel() {

    private val _adoptionUiState = MutableStateFlow(AdoptionUiState())
    val adoptionUiState = _adoptionUiState.asStateFlow()

    private val _adoptionFilterState = MutableStateFlow(AdoptionFilterState())
    val adoptionFilterState = _adoptionFilterState.asStateFlow()

    private val _selectedFilterOptions = MutableStateFlow(AdoptionFilterOptionState())
    val selectedFilterOptions = _selectedFilterOptions.asStateFlow()

    private var pageNo = 1


    init {
        val req = adoptionFilterState.value.filterOptions.run {
            AbandonmentPublicRequest(
                bgnde = selectedArea.start,
                endde = selectedArea.end,
                upkind = selectedUpKind.upKindCd,
                upr_cd = selectedSido.orgCd,
                org_cd = selectedSigungu.orgCd,
                state = selectedState.value,
                neuter_yn = selectedNeuter.value,
                pageNo = pageNo
            )
        }
        getAbandonmentPublic(req)
        getSido()
    }

    fun onEvent(event: AdoptionEvent) {
        when (event) {
            AdoptionEvent.Refresh -> {
                pageNo = 1
                getAbandonmentPublic(adoptionFilterState.value.filterOptions.run {
                    AbandonmentPublicRequest(
                        bgnde = selectedArea.start,
                        endde = selectedArea.end,
                        upkind = selectedUpKind.upKindCd,
                        upr_cd = selectedSido.orgCd,
                        org_cd = selectedSigungu.orgCd,
                        state = selectedState.value,
                        neuter_yn = selectedNeuter.value,
                    )
                })
            }

            is AdoptionEvent.LoadMore -> {
                pageNo++
                getLoadMore(adoptionFilterState.value.filterOptions.run {
                    AbandonmentPublicRequest(
                        bgnde = selectedArea.start,
                        endde = selectedArea.end,
                        upkind = selectedUpKind.upKindCd,
                        upr_cd = selectedSido.orgCd,
                        org_cd = selectedSigungu.orgCd,
                        state = selectedState.value,
                        neuter_yn = selectedNeuter.value,
                        pageNo = pageNo
                    )
                })
            }

            is AdoptionEvent.SelectedCategory -> {
                _adoptionFilterState.update { state ->
                    val updateCategories = state.selectedCategories.toMutableList().apply {
                        if (!contains(event.category)) {
                            add(event.category)
                        } else {
                            remove(event.category)
                        }
                    }
                    Log.d("sjh", "updateCategories : ${updateCategories}")
                    state.copy(
                        selectedCategories = updateCategories
                    )
                }
            }

            is AdoptionEvent.FilterBottomSheetOpen -> {
                _adoptionFilterState.update {
                    it.copy(
                        filterBottomSheetState = event.bottomSheetState,
                    )
                }
                _selectedFilterOptions.update {
                    adoptionFilterState.value.filterOptions
                }
            }

            is AdoptionEvent.SelectedSigungu -> {
                _selectedFilterOptions.update {
                    it.copy(
                        selectedSigungu = event.sigungu
                    )
                }
            }

            is AdoptionEvent.SelectedSido -> {

                _selectedFilterOptions.update {
                    it.copy(
                        selectedSido = event.sido, selectedSigungu = Sigungu(
                            //Sigungu 에선 시도는 uprCd..
                            uprCd = event.sido.orgCd
                        )
                    )
                }

                event.sido.orgCd?.let {
                    getSigungu(SigunguRequest(upr_cd = it))
                }
            }

            is AdoptionEvent.SelectedArea -> {
                _selectedFilterOptions.update {
                    it.copy(
                        selectedArea = event.area
                    )
                }
            }

            is AdoptionEvent.SelectedUpKind -> {
                _selectedFilterOptions.update {
                    it.copy(
                        selectedUpKind = event.upKind
                    )
                }
            }

            is AdoptionEvent.SelectedState -> {
                _selectedFilterOptions.update {
                    it.copy(
                        selectedState = event.state
                    )
                }
            }

            is AdoptionEvent.SelectedNeuter -> {
                _selectedFilterOptions.update {
                    it.copy(
                        selectedNeuter = event.neuter
                    )
                }
            }

            AdoptionEvent.Apply -> {
                pageNo = 1
                _adoptionFilterState.update {
                    it.copy(
                        filterOptions = selectedFilterOptions.value,
                        filterBottomSheetState = FilterBottomSheetState.HIDE
                    )
                }
                getAbandonmentPublic(adoptionFilterState.value.filterOptions.run {
                    AbandonmentPublicRequest(
                        bgnde = selectedArea.start,
                        endde = selectedArea.end,
                        upkind = selectedUpKind.upKindCd,
                        upr_cd = selectedSido.orgCd,
                        org_cd = selectedSigungu.orgCd,
                        state = selectedState.value,
                        neuter_yn = selectedNeuter.value,
                    )
                })

            }

            AdoptionEvent.SelectedInit -> {
                _selectedFilterOptions.update {
                    AdoptionFilterOptionState()
                }
            }
        }
    }


    private fun getAbandonmentPublic(request: AbandonmentPublicRequest = AbandonmentPublicRequest()) {
        viewModelScope.launch {
            adoptionRepository.getAbandonmentPublic(request).collect { result ->
                when (result) {
                    is Response.Failure -> {
                        result.e.printStackTrace()
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

    private fun getSido() {
        viewModelScope.launch {
            adoptionRepository.getSido(SidoRequest()).collect { result ->
                when (result) {
                    is Response.Failure -> {
                        result.e.printStackTrace()
                    }

                    Response.Loading -> {

                    }

                    is Response.Success -> {
                        _adoptionFilterState.update {
                            Log.d("sjh", "result.data : ${result.data}")
                            it.copy(
                                sidoList = result.data,
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
                        result.e.printStackTrace()
                    }

                    Response.Loading -> {}
                    is Response.Success -> {
                        _adoptionFilterState.update {
                            Log.d("sjh", "result.data : ${result.data}")
                            it.copy(
                                sigunguList = result.data
                            )
                        }
                    }
                }

            }
        }
    }
}