package kr.sjh.feature.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.sjh.core.model.ReportForm
import kr.sjh.data.repository.CommentRepository
import javax.inject.Inject

data class ReportUiState(
    val loading: Boolean = false,
    val isSuccessful: Boolean = false,
    val error: Exception? = null
)

@HiltViewModel
class ReportViewModel @Inject constructor(private val reportRepository: CommentRepository) :
    ViewModel() {
    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState = _uiState.asStateFlow()

    fun report(reportForm: ReportForm) {
        _uiState.value = _uiState.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                reportRepository.reportUsers(reportForm)
                _uiState.value =
                    _uiState.value.copy(loading = false, isSuccessful = true, error = null)
            } catch (e: Exception) {
                _uiState.value =
                    _uiState.value.copy(loading = false, isSuccessful = false, error = e)
            }
        }
    }
}