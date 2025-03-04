package kr.sjh.feature.adoption.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.sjh.core.common.snackbar.SnackBarManager
import kr.sjh.core.ktor.model.request.PetRequest
import kr.sjh.data.repository.AdoptionRepository
import kr.sjh.feature.adoption.state.AdoptionEvent
import kr.sjh.feature.adoption.state.AdoptionUiState
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class AdoptionViewModel @Inject constructor(
    private val adoptionRepository: AdoptionRepository,
) : ViewModel() {
    private val snackBarManager = SnackBarManager
    private val _adoptionUiState = MutableStateFlow(AdoptionUiState())
    val adoptionUiState = _adoptionUiState.asStateFlow()

    private val _petRequest = MutableSharedFlow<PetRequest>(replay = 1)

    init {
        _petRequest.flatMapLatest { req ->
            getPets(req)
        }.launchIn(viewModelScope)
    }

    private fun getPets(req: PetRequest) = adoptionRepository.getPets(req).onStart {
        _adoptionUiState.update {
            it.copy(isRefreshing = req.pageNo == 1, isMore = req.pageNo > 1)
        }
    }.retryWhen { cause, attempt ->
        if (attempt < 3) {
            delay(1000)
            true
        } else {
            false
        }
    }.catch { e ->
        // 모든 재시도가 실패한 경우 처리
        snackBarManager.showMessage("서버 상태가 원활하지 않아 데이터를 불러오지 못했습니다.")
        _adoptionUiState.update {
            it.copy(isRefreshing = false, isMore = false)
        }
    }.onEach { pets ->
        if (req.pageNo == 1) {
            // 펫 데이터 초기 로드 및 새로고침
            _adoptionUiState.update {
                it.copy(pets = pets, isRefreshing = false)
            }
        } else {
            // 펫 데이터 추가 로드
            _adoptionUiState.update {
                val newPets = (it.pets + pets).distinctBy { it.desertionNo }
                it.copy(pets = newPets, isMore = false)
            }
        }
    }.flowOn(Dispatchers.IO)

    fun onEvent(event: AdoptionEvent) {
        when (event) {
            is AdoptionEvent.LoadMore -> {
                viewModelScope.launch {
                    _petRequest.emit(
                        _petRequest.replayCache.first()
                            .copy(pageNo = _petRequest.replayCache.first().pageNo + 1)
                    )
                }
            }

            is AdoptionEvent.Refresh -> {
                viewModelScope.launch {
                    _petRequest.emit(event.req)
                }
            }
        }
    }
}
