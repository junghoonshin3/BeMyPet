package kr.sjh.feature.adoption.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.sjh.core.common.snackbar.SnackBarManager
import kr.sjh.core.ktor.model.request.AbandonmentPublicRequest
import kr.sjh.core.ktor.model.request.SidoRequest
import kr.sjh.core.ktor.model.request.SigunguRequest
import kr.sjh.core.model.Response
import kr.sjh.data.repository.AdoptionRepository
import kr.sjh.feature.adoption.navigation.Adoption
import kr.sjh.feature.adoption.state.AdoptionEvent
import kr.sjh.feature.adoption.state.AdoptionFilterState
import kr.sjh.feature.adoption.state.AdoptionUiState
import javax.inject.Inject

@HiltViewModel
class AdoptionViewModel @Inject constructor(
    private val adoptionRepository: AdoptionRepository,
) : ViewModel() {

    private val _adoptionUiState = MutableStateFlow(AdoptionUiState())
    val adoptionUiState = _adoptionUiState.asStateFlow()

    private val _adoptionFilterState = MutableStateFlow(AdoptionFilterState())
    val adoptionFilterState = _adoptionFilterState.asStateFlow()

    init {
        init()
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
                val selectedCategory = event.category
                _adoptionFilterState.update {
                    it.copy(
                        selectedCategory = selectedCategory
                    )
                }
            }

            AdoptionEvent.InitCategory -> {
                //초기화
                _adoptionFilterState.update {
                    AdoptionFilterState()
                }
                init()
            }

            AdoptionEvent.LoadSido -> {
                getSido()
            }

            is AdoptionEvent.LoadSigungu -> {
                //전체 선택시
                getSigungu(
                    SigunguRequest(
                        upr_cd = event.sido.orgCd
                    )
                )
            }

            is AdoptionEvent.SelectedNeuter -> {
                _adoptionFilterState.update {
                    it.copy(
                        selectedNeuter = event.neuter
                    )
                }
                getAbandonmentPublic(
                    _adoptionFilterState.value.toAbandonmentPublicRequest()
                )
            }

            is AdoptionEvent.SelectedUpKind -> {
                _adoptionFilterState.update {
                    it.copy(
                        selectedUpKind = event.upKind
                    )
                }
                getAbandonmentPublic(
                    _adoptionFilterState.value.toAbandonmentPublicRequest()
                )
            }

            is AdoptionEvent.SelectedLocation -> {
                _adoptionFilterState.update {
                    it.copy(
                        selectedSido = event.sido, selectedSigungu = event.sigungu
                    )
                }
                getAbandonmentPublic(
                    _adoptionFilterState.value.toAbandonmentPublicRequest()
                )
            }

            is AdoptionEvent.SelectedDateRange -> {
                _adoptionFilterState.update {
                    it.copy(
                        selectedStartDate = event.startDate,
                        selectedEndDate = event.endDate

                    )
                }
                getAbandonmentPublic(
                    _adoptionFilterState.value.toAbandonmentPublicRequest()
                )
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

    private fun init() {
        getAbandonmentPublic(_adoptionFilterState.value.toAbandonmentPublicRequest())
        getSido()
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
                                pets = it.pets.plus(result.data.first)
                                    .distinctBy { it.desertionNo },
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
                        result.e.message?.let { msg ->
                            SnackBarManager.showMessage(msg)
                        }
                        _adoptionFilterState.update {
                            it.copy(
                                isLocationError = true, isLocationLoading = false
                            )
                        }
                    }

                    Response.Loading -> {
                        _adoptionFilterState.update {
                            it.copy(
                                isLocationError = false, isLocationLoading = true
                            )
                        }
                    }

                    is Response.Success -> {
                        _adoptionFilterState.update {
                            it.copy(
                                isLocationLoading = false,
                                isLocationError = false,
                                sidoList = result.data
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getSigungu(req: SigunguRequest) {
        viewModelScope.launch {
            if (req.upr_cd == null) {
                //전체인 경우
                _adoptionFilterState.update {
                    it.copy(
                        sigunguList = emptyList()
                    )
                }
                return@launch
            }
            adoptionRepository.getSigungu(req).collect { result ->
                when (result) {
                    is Response.Failure -> {
                        result.e.message?.let { msg ->
                            SnackBarManager.showMessage(msg)
                        }
                        _adoptionFilterState.update {
                            it.copy(
                                isLocationError = true, isLocationLoading = false
                            )
                        }
                    }

                    Response.Loading -> {
                        _adoptionFilterState.update {
                            it.copy(
                                isLocationError = false, isLocationLoading = true
                            )
                        }
                    }

                    is Response.Success -> {
                        _adoptionFilterState.update {
                            it.copy(
                                sigunguList = result.data,
                                isLocationError = false,
                                isLocationLoading = false
                            )
                        }
                    }
                }
            }
        }
    }
}
