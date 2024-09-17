package kr.sjh.feature.adoption.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.sjh.core.ktor.model.ApiResult
import kr.sjh.core.ktor.model.request.AbandonmentPublicRequest
import kr.sjh.core.ktor.repository.AdoptionService
import kr.sjh.core.model.Response
import kr.sjh.data.repository.AdoptionRepository
import kr.sjh.feature.adoption.state.AdoptionEvent
import kr.sjh.feature.adoption.state.AdoptionUiState
import javax.inject.Inject

@HiltViewModel
class AdoptionViewModel @Inject constructor(
    private val adoptionRepository: AdoptionRepository
) : ViewModel() {

    private val _adoptionUiState = MutableStateFlow(AdoptionUiState())
    val adoptionUiState = _adoptionUiState.asStateFlow()

    init {
        getAbandonmentPublic(
            AbandonmentPublicRequest(
                pageNo = 1, numOfRows = 20
            )
        )
    }

    fun onEvent(event: AdoptionEvent) {
        when (event) {
            AdoptionEvent.Refresh -> {
                getAbandonmentPublic()
            }
        }
    }


    private fun getAbandonmentPublic(request: AbandonmentPublicRequest = AbandonmentPublicRequest()) {
        viewModelScope.launch {
            adoptionRepository.getAbandonmentPublic(request).collect { result ->
                when (result) {
                    is Response.Failure -> {
                        Log.d("sjh", "Failure")
                        result.e.printStackTrace()
                        _adoptionUiState.update {
                            //TODO 에러가 난 경우 팝업 or 바텀 시트 다이얼로그 생성
                            it.copy(
                                false
                            )
                        }
                    }

                    Response.Loading -> {
                        Log.d("sjh", "Loading")
                        _adoptionUiState.update {
                            it.copy(
                                true
                            )
                        }
                    }

                    is Response.Success -> {
                        Log.d("sjh", "?")
                        _adoptionUiState.update {
                            it.copy(
                                false, result.data
                            )
                        }
                    }
                }

            }
        }
    }
}