package kr.sjh.feature.adoption.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kr.sjh.core.model.adoption.filter.Sido
import kr.sjh.core.model.adoption.filter.Sigungu
import kr.sjh.data.repository.AdoptionRepository
import kr.sjh.feature.adoption.state.AdoptionEvent
import kr.sjh.feature.adoption.state.AdoptionFilterState
import kr.sjh.feature.adoption.state.FilterUiState
import kr.sjh.feature.adoption.state.AdoptionUiState
import kr.sjh.feature.adoption.state.FilterEvent
import javax.inject.Inject

@HiltViewModel
class AdoptionViewModel @Inject constructor(
    private val adoptionRepository: AdoptionRepository,
) : ViewModel() {

    private val _adoptionUiState = MutableStateFlow(AdoptionUiState())
    val adoptionUiState = _adoptionUiState.asStateFlow()

    fun onEvent(event: AdoptionEvent) {
        when (event) {
            AdoptionEvent.LoadMore -> {}
            AdoptionEvent.Refresh -> {}
        }
    }
}
