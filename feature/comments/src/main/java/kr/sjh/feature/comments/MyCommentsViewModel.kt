package kr.sjh.feature.comments

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.sjh.core.common.snackbar.SnackBarManager
import kr.sjh.core.model.Comment
import kr.sjh.data.repository.CommentRepository
import kr.sjh.feature.comments.navigation.MyComments

@Stable
data class MyCommentsUiState(
    val loading: Boolean = false,
    val comments: List<Comment> = emptyList(),
    val pendingDeleteComment: Comment? = null,
)

sealed interface MyCommentsEvent {
    data object Load : MyCommentsEvent
    data class RequestDelete(val comment: Comment) : MyCommentsEvent
    data object DismissDeleteDialog : MyCommentsEvent
    data class ConfirmDelete(val commentId: String) : MyCommentsEvent
}

@HiltViewModel
class MyCommentsViewModel @Inject constructor(
    private val commentRepository: CommentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val args = savedStateHandle.toRoute<MyComments>()
    val userId: String = args.userId

    private val _uiState = MutableStateFlow(MyCommentsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        onEvent(MyCommentsEvent.Load)
    }

    fun onEvent(event: MyCommentsEvent) {
        when (event) {
            MyCommentsEvent.Load -> loadMyComments()
            is MyCommentsEvent.RequestDelete -> {
                _uiState.update { state ->
                    state.copy(pendingDeleteComment = event.comment)
                }
            }

            MyCommentsEvent.DismissDeleteDialog -> {
                _uiState.update { state ->
                    state.copy(pendingDeleteComment = null)
                }
            }

            is MyCommentsEvent.ConfirmDelete -> deleteComment(event.commentId)
        }
    }

    private fun loadMyComments() {
        val normalizedUserId = userId.trim()
        if (normalizedUserId.isBlank()) {
            _uiState.update { state ->
                state.copy(loading = false, comments = emptyList())
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(loading = true)
            }

            runCatching {
                commentRepository.getMyComments(normalizedUserId)
            }.onSuccess { comments ->
                _uiState.update { state ->
                    state.copy(loading = false, comments = comments)
                }
            }.onFailure {
                _uiState.update { state ->
                    state.copy(loading = false)
                }
                SnackBarManager.showMessage("내 댓글 목록을 불러오지 못했어요.")
            }
        }
    }

    private fun deleteComment(commentId: String) {
        val normalizedCommentId = commentId.trim()
        if (normalizedCommentId.isBlank()) {
            _uiState.update { state ->
                state.copy(pendingDeleteComment = null)
            }
            return
        }

        viewModelScope.launch {
            commentRepository.deleteComment(
                commentId = normalizedCommentId,
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(pendingDeleteComment = null)
                    }
                    loadMyComments()
                    SnackBarManager.showMessage("댓글을 삭제했어요.")
                },
                onFailure = {
                    _uiState.update { state ->
                        state.copy(pendingDeleteComment = null)
                    }
                    SnackBarManager.showMessage("댓글 삭제에 실패했어요.")
                }
            )
        }
    }
}
