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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.sjh.core.model.BlockUser
import kr.sjh.core.model.Comment
import kr.sjh.core.model.User
import kr.sjh.data.repository.CommentRepository
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
    val blockedUser: List<String> = emptyList(),
    val textFieldValue: TextFieldValue = TextFieldValue(),
    val isBlockedUser: Boolean = false,

    )

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val commentRepository: CommentRepository, savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val arg = savedStateHandle.toRoute<Comments>()

    private val _uiState = MutableStateFlow(CommentUiState())
    val uiState = _uiState.asStateFlow()

    private val _sideEffect = Channel<CommentSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        combine(
            commentRepository.getComments(arg.noticeNo), commentRepository.getBlockUsers(arg.userId)
        ) { comments, blockUsers ->
            val blockedList = blockUsers.map { it.blockedUser }
            val newComments = comments.filterNot { comment ->
                comment.userId in blockedList
            }
            _uiState.update {
                it.copy(comments = newComments, blockedUser = blockedList, loading = false)
            }
        }.onStart {
            _uiState.update {
                it.copy(
                    loading = true
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun isBlockedUser(comment: Comment) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isBlockedUser = it.blockedUser.contains(comment.userId))
            }
        }
    }

    private fun unBlock(blockerId: String, blockedId: String) {
        viewModelScope.launch {
            commentRepository.unblockUser(blockerId, blockedId, {
                _uiState.update {
                    it.copy(isBlockedUser = false)
                }
            }, { e -> e.printStackTrace() })
        }
    }

    private fun delete(id: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            commentRepository.deleteComment(id, onSuccess = onSuccess, onFailure = onFailure)
        }
    }


    private fun send(comment: Comment) {
        if (comment.content.isBlank()) return  // 빈 댓글 방지
        viewModelScope.launch {
            commentRepository.insertComment(comment = comment)
        }
        _uiState.update {
            it.copy(textFieldValue = TextFieldValue(), currentComment = null)
        }
    }

    private fun edit(comment: Comment) {
        viewModelScope.launch {
            commentRepository.updateComment(comment = comment)
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
            commentRepository.blockUser(BlockUser(
                blockerUser = blockerId, blockedUser = blockedId
            ),
                { _uiState.update { it.copy(isBlockedUser = true) } },
                { e -> e.printStackTrace() })
        }
    }

    fun onEvent(event: CommentEvent) {
        when (event) {
            is CommentEvent.ShowDeleteDialog -> {
                _uiState.update {
                    it.copy(
                        currentComment = event.comment, isDeleteDialogVisible = true
                    )
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

            is CommentEvent.Report -> {
                _uiState.update {
                    it.copy(currentComment = event.comment)
                }
                isBlockedUser(event.comment)
                showBottomSheet()
            }

            is CommentEvent.Send -> {
                val comment = event.comment.copy(postId = arg.noticeNo)
                send(comment)
            }

            is CommentEvent.SelectReportType -> {
                viewModelScope.launch {
                    _sideEffect.send(
                        CommentSideEffect.NavigateToReport(
                            event.reportType, event.comment, event.user
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
                    it.copy(
                        isEditDialogVisible = true
                    )
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
                    it.copy(
                        isEditDialogVisible = false
                    )
                }
            }

            is CommentEvent.Edit -> {
                edit(event.comment)
            }

            is CommentEvent.Delete -> {
                delete(uiState.value.currentComment?.id.toString(), { id ->
                    _uiState.update {
                        it.copy(comments = it.comments.toMutableList().apply {
                            removeIf {
                                it.id.toString() == id
                            }
                        })
                    }
                    dismissDeleteDialog()
                }, { e -> e.printStackTrace() })
            }

            CommentEvent.DismissDeleteDialog -> {
                dismissDeleteDialog()
            }

            is CommentEvent.UnBlock -> {
                unBlock(event.blockerId, event.blockedId)
                hideBottomSheet()
            }
        }
    }

    private fun dismissDeleteDialog() {
        _uiState.update {
            it.copy(
                isDeleteDialogVisible = false, currentComment = null
            )
        }
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
            _sideEffect.send(
                CommentSideEffect.ShowBottomSheet
            )
        }
    }

    private fun hideBottomSheet() {
        viewModelScope.launch {
            _sideEffect.send(
                CommentSideEffect.HideBottomSheet
            )
        }
        _uiState.update {
            it.copy(currentComment = null)
        }
    }
}