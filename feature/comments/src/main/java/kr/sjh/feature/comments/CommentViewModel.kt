package kr.sjh.feature.comments

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.sjh.core.model.BlockUser
import kr.sjh.core.model.Comment
import kr.sjh.core.model.ReportType
import kr.sjh.data.repository.CommentRepository
import kr.sjh.feature.comments.navigation.CommentAction
import kr.sjh.feature.comments.navigation.CommentEvent
import kr.sjh.feature.comments.navigation.CommentSideEffect
import kr.sjh.feature.comments.navigation.Comments
import javax.inject.Inject

@Stable
data class CommentUiState(
    val loading: Boolean = false,
    val comments: List<Comment> = emptyList(),
    val isEditDialogVisible: Boolean = false,
    val isDeleteDialogVisible: Boolean = false,
    val isStartEditing: Boolean = false,
    val currentComment: Comment? = null,
    val textFieldValue: TextFieldValue = TextFieldValue(),
)

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val commentRepository: CommentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val arg = savedStateHandle.toRoute<Comments>()

    private val _uiState = MutableStateFlow(CommentUiState())
    val uiState = _uiState.asStateFlow()

    private val _sideEffect = Channel<CommentSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        loadComments(arg.noticeNo, arg.userId)
    }

    private fun loadComments(noticeNo: String = arg.noticeNo, userId: String = arg.userId) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            runCatching {
                commentRepository.getComments(noticeNo, userId)
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(comments = result, loading = false)
                }
            }.onFailure { e ->
                e.printStackTrace()
                _uiState.update { state ->
                    state.copy(loading = false)
                }
            }
        }
    }

    private fun delete(id: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            commentRepository.deleteComment(id, onSuccess = onSuccess, onFailure = onFailure)
        }
    }

    private fun send(comment: Comment) {
        if (comment.content.isBlank()) return
        viewModelScope.launch {
            commentRepository.insertComment(comment = comment)
            loadComments()
        }
        _uiState.update {
            it.copy(textFieldValue = TextFieldValue(), currentComment = null)
        }
    }

    private fun edit(comment: Comment) {
        viewModelScope.launch {
            commentRepository.updateComment(comment = comment)
            loadComments()
            _uiState.update {
                it.copy(
                    textFieldValue = TextFieldValue(),
                    currentComment = null,
                    isStartEditing = false,
                )
            }
        }
    }

    private fun blockUser(blockerId: String, blockedId: String) {
        viewModelScope.launch {
            commentRepository.blockUser(
                BlockUser(blockerUser = blockerId, blockedUser = blockedId),
                {},
                { e -> e.printStackTrace() }
            )
        }
    }

    fun onEvent(event: CommentEvent) {
        when (event) {
            is CommentEvent.OpenActionSheet -> {
                _uiState.update { it.copy(currentComment = event.comment) }
                showBottomSheet()
            }

            CommentEvent.CloseActionSheet -> {
                hideBottomSheet(clearCurrentComment = shouldClearCurrentCommentOnSheetClose())
            }

            is CommentEvent.SelectAction -> {
                handleAction(event.action, event.user)
            }

            is CommentEvent.ShowDeleteDialog -> {
                _uiState.update {
                    it.copy(currentComment = event.comment, isDeleteDialogVisible = true)
                }
            }

            is CommentEvent.StartEditing -> {
                viewModelScope.launch {
                    _sideEffect.send(CommentSideEffect.ShowKeyboard)
                }
                _uiState.update {
                    it.copy(
                        isStartEditing = true,
                        currentComment = event.comment,
                        textFieldValue = TextFieldValue(
                            text = event.comment.content,
                            selection = TextRange(event.comment.content.length)
                        )
                    )
                }
            }

            is CommentEvent.Send -> {
                val comment = event.comment.copy(noticeNo = arg.noticeNo)
                send(comment)
            }

            is CommentEvent.SelectReportType -> {
                viewModelScope.launch {
                    _sideEffect.send(
                        CommentSideEffect.NavigateToReport(
                            event.reportType,
                            event.comment,
                            event.user
                        )
                    )
                }
            }

            is CommentEvent.Block -> {
                blockUser(event.blockerId, event.blockedId)
                hideBottomSheet()
            }

            is CommentEvent.OnChangeText -> {
                onTextChange(event.textField)
            }

            CommentEvent.OnClearText -> {
                _uiState.update {
                    it.copy(
                        currentComment = it.currentComment?.copy(content = ""),
                        textFieldValue = TextFieldValue()
                    )
                }
            }

            CommentEvent.OnKeyboardClosedDuringEdit -> {
                _uiState.update {
                    it.copy(isEditDialogVisible = true)
                }
            }

            CommentEvent.CancelEditing -> {
                _uiState.update {
                    it.copy(
                        isStartEditing = false,
                        isEditDialogVisible = false,
                        currentComment = null,
                        textFieldValue = TextFieldValue()
                    )
                }
            }

            CommentEvent.StayEditing -> {
                _uiState.update {
                    it.copy(isEditDialogVisible = false)
                }
            }

            is CommentEvent.Edit -> {
                edit(event.comment)
            }

            is CommentEvent.Delete -> {
                delete(event.commentId, { _ ->
                    loadComments()
                    dismissDeleteDialog()
                }, { e -> e.printStackTrace() })
            }

            CommentEvent.DismissDeleteDialog -> {
                dismissDeleteDialog()
            }
        }
    }

    private fun handleAction(action: CommentAction, user: kr.sjh.core.model.User) {
        val comment = _uiState.value.currentComment ?: return
        when (action) {
            CommentAction.Edit -> {
                hideBottomSheet(clearCurrentComment = false)
                onEvent(CommentEvent.StartEditing(comment))
            }

            CommentAction.Delete -> {
                _uiState.update {
                    it.copy(isDeleteDialogVisible = true)
                }
                hideBottomSheet(clearCurrentComment = false)
            }

            CommentAction.ReportComment -> {
                viewModelScope.launch {
                    _sideEffect.send(
                        CommentSideEffect.NavigateToReport(
                            ReportType.Comment,
                            comment,
                            user
                        )
                    )
                }
            }

            CommentAction.ReportUser -> {
                viewModelScope.launch {
                    _sideEffect.send(
                        CommentSideEffect.NavigateToReport(
                            ReportType.User,
                            comment,
                            user
                        )
                    )
                }
            }

            CommentAction.BlockUser -> {
                blockUser(user.id, comment.userId)
                hideBottomSheet()
            }
        }
    }

    private fun dismissDeleteDialog() {
        _uiState.update {
            it.copy(isDeleteDialogVisible = false, currentComment = null)
        }
    }

    private fun shouldClearCurrentCommentOnSheetClose(): Boolean {
        val state = _uiState.value
        return !state.isStartEditing && !state.isDeleteDialogVisible
    }

    private fun onTextChange(textFieldValue: TextFieldValue) {
        val comment = _uiState.value.currentComment ?: Comment()
        _uiState.update {
            it.copy(
                currentComment = comment.copy(content = textFieldValue.text),
                textFieldValue = textFieldValue
            )
        }
    }

    private fun showBottomSheet() {
        viewModelScope.launch {
            _sideEffect.send(CommentSideEffect.ShowBottomSheet)
        }
    }

    private fun hideBottomSheet(clearCurrentComment: Boolean = true) {
        viewModelScope.launch {
            _sideEffect.send(CommentSideEffect.HideBottomSheet)
        }
        if (clearCurrentComment) {
            _uiState.update {
                it.copy(currentComment = null)
            }
        }
    }
}
