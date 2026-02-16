package kr.sjh.feature.comments.navigation

import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kr.sjh.core.model.Comment
import kr.sjh.core.model.ReportType
import kr.sjh.core.model.User

@Serializable
data class Comments(val noticeNo: String, val userId: String)

enum class CommentAction {
    Edit,
    Delete,
    ReportComment,
    ReportUser,
    BlockUser,
}

sealed class CommentEvent {
    data class OpenActionSheet(val comment: Comment) : CommentEvent()
    data object CloseActionSheet : CommentEvent()
    data class SelectAction(val action: CommentAction, val user: User) : CommentEvent()
    data class StartEditing(val comment: Comment) : CommentEvent()
    data class ShowDeleteDialog(val comment: Comment) : CommentEvent()
    data object DismissDeleteDialog : CommentEvent()
    data object Delete : CommentEvent()
    data class Send(val comment: Comment) : CommentEvent()
    data class Edit(val comment: Comment) : CommentEvent()
    data class OnChangeText(val textField: TextFieldValue) : CommentEvent()
    data object OnClearText : CommentEvent()
    data class Block(
        val blockerId: String, val blockedId: String, val rawUserMetaData: JsonObject
    ) : CommentEvent()

    data object OnKeyboardClosedDuringEdit : CommentEvent()
    data class SelectReportType(val reportType: ReportType, val comment: Comment, val user: User) :
        CommentEvent()

    data object CancelEditing : CommentEvent()
    data object StayEditing : CommentEvent()
}


sealed class CommentSideEffect {
    data object HideBottomSheet : CommentSideEffect()
    data object ShowBottomSheet : CommentSideEffect()
    data object ShowKeyboard : CommentSideEffect()
    data object HideKeyboard : CommentSideEffect()
    data class NavigateToReport(val reportType: ReportType, val comment: Comment, val user: User) :
        CommentSideEffect()

}