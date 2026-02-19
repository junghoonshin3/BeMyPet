package kr.sjh.feature.comments

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.composables.core.ModalBottomSheet
import com.composables.core.ModalBottomSheetState
import com.composables.core.Scrim
import com.composables.core.Sheet
import com.composables.core.SheetDetent
import com.composables.core.rememberModalBottomSheetState
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.BeMyPetTopAppBar
import kr.sjh.core.designsystem.components.LoadingComponent
import kr.sjh.core.designsystem.components.Title
import kr.sjh.core.designsystem.theme.RoundedCorner12
import kr.sjh.core.designsystem.theme.RoundedCorner18
import kr.sjh.core.designsystem.theme.RoundedCornerBottom24
import kr.sjh.core.designsystem.theme.RoundedCornerTop24
import kr.sjh.core.model.Comment
import kr.sjh.core.model.Role
import kr.sjh.core.model.SessionState
import kr.sjh.core.model.User
import kr.sjh.core.model.ReportType
import kr.sjh.feature.comments.navigation.CommentAction
import kr.sjh.feature.comments.navigation.CommentEvent
import kr.sjh.feature.comments.navigation.CommentSideEffect
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

private val CommentInputShape = RoundedCornerShape(20.dp)
private val CommentItemShape = RoundedCornerShape(16.dp)
private val CommentInputTextStyle = TextStyle(color = Color.Black)
private val CommentDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분")

@Composable
fun CommentRoute(
    modifier: Modifier = Modifier,
    session: SessionState,
    onBack: () -> Unit,
    navigateToReport: (ReportType, Comment, User) -> Unit,
    commentViewModel: CommentViewModel = hiltViewModel()
) {
    val uiState by commentViewModel.uiState.collectAsStateWithLifecycle()
    val peek = remember {
        SheetDetent("peek", calculateDetentHeight = { containerHeight, _ ->
            containerHeight * 0.42f
        })
    }

    val bottomSheetState =
        rememberModalBottomSheetState(SheetDetent.Hidden, listOf(peek, SheetDetent.Hidden))

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val latestKeyboardController by rememberUpdatedState(keyboardController)
    val latestNavigateToReport by rememberUpdatedState(navigateToReport)

    LaunchedEffect(commentViewModel, bottomSheetState, peek) {
        commentViewModel.sideEffect.collect { event ->
            when (event) {
                CommentSideEffect.HideBottomSheet -> {
                    bottomSheetState.animateTo(SheetDetent.Hidden)
                }

                CommentSideEffect.ShowBottomSheet -> {
                    bottomSheetState.animateTo(peek)
                }

                CommentSideEffect.HideKeyboard -> {
                    latestKeyboardController?.hide()
                }

                CommentSideEffect.ShowKeyboard -> {
                    latestKeyboardController?.show()
                }

                is CommentSideEffect.NavigateToReport -> {
                    bottomSheetState.animateTo(SheetDetent.Hidden)
                    latestNavigateToReport(event.reportType, event.comment, event.user)
                }
            }
        }
    }

    when (session) {
        is SessionState.Authenticated -> {
            val user = session.user
            CommentScreen(
                modifier = modifier
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        })
                    }
                    .imePadding(),
                uiState = uiState,
                bottomSheetState = bottomSheetState,
                user = user,
                onBack = onBack,
                onEvent = commentViewModel::onEvent,
            )
        }

        else -> Unit
    }
}

@Composable
fun CommentScreen(
    modifier: Modifier = Modifier,
    uiState: CommentUiState,
    bottomSheetState: ModalBottomSheetState,
    user: User,
    onEvent: (CommentEvent) -> Unit,
    onBack: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.comments.size) {
        if (uiState.comments.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    if (uiState.isStartEditing) {
        KeyboardWatcher {
            Log.d("sjh", "keyboard 닫힘")
            onEvent(CommentEvent.OnKeyboardClosedDuringEdit)
        }
    }

    if (uiState.isEditDialogVisible) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("편집 중단") },
            text = { Text("편집을 중단하시겠어요?") },
            confirmButton = {
                TextButton(onClick = { onEvent(CommentEvent.CancelEditing) }) {
                    Text("예", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(CommentEvent.StayEditing) }) {
                    Text("아니오", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        )
    }

    if (uiState.isDeleteDialogVisible) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("삭제") },
            text = { Text("정말로 삭제하시겠어요?") },
            confirmButton = {
                TextButton(onClick = { onEvent(CommentEvent.Delete) }) {
                    Text("예", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(CommentEvent.DismissDeleteDialog) }) {
                    Text("아니오", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        )
    }

    CommentActionBottomSheet(
        bottomSheetState = bottomSheetState,
        uiState = uiState,
        user = user,
        onEvent = onEvent
    )

    Column(modifier = modifier.background(MaterialTheme.colorScheme.background)) {
        BeMyPetTopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerBottom24)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerBottom24)
                .clip(RoundedCornerBottom24),
            title = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.baseline_arrow_back_24),
                        contentDescription = "back",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Title(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    title = "댓글",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        )

        if (uiState.loading) {
            LoadingComponent()
            return@Column
        }

        if (uiState.comments.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "댓글이 없어요.\n궁금한 점이나 후기를 작성해보세요!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 8.dp),
                state = listState,
                reverseLayout = true,
                verticalArrangement = Arrangement.Top
            ) {
                items(uiState.comments, key = { it.id }) { comment ->
                    CommentItem(
                        comment = comment,
                        user = user,
                        modifier = Modifier.fillMaxWidth(),
                        onOpenActionSheet = {
                            onEvent(CommentEvent.OpenActionSheet(comment))
                        }
                    )
                }
            }
        }

        CommentInput(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, bottom = 8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CommentInputShape)
                .padding(horizontal = 12.dp, vertical = 5.dp),
            isEdit = uiState.isStartEditing,
            textField = uiState.textFieldValue,
            onTextChange = { onEvent(CommentEvent.OnChangeText(it)) },
            onClearText = { onEvent(CommentEvent.OnClearText) },
            onSend = {
                val comment = uiState.currentComment ?: Comment()
                onEvent(
                    if (uiState.isStartEditing) {
                        CommentEvent.Edit(comment)
                    } else {
                        CommentEvent.Send(
                            comment.copy(
                                content = uiState.textFieldValue.text,
                                userId = user.id,
                            )
                        )
                    }
                )
            }
        )
    }
}

@Composable
fun CommentInput(
    modifier: Modifier = Modifier,
    isEdit: Boolean = false,
    textField: TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit,
    onClearText: () -> Unit,
    onSend: () -> Unit,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        TextField(
            value = textField,
            onValueChange = {
                onTextChange(it.copy(selection = TextRange(it.text.length)))
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Default),
            placeholder = { Text("댓글을 입력하세요.") },
            colors = TextFieldDefaults.colors(
                cursorColor = Color.Black,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                selectionColors = TextSelectionColors(Color.Black, Color.Black)
            ),
            textStyle = CommentInputTextStyle,
            modifier = Modifier
                .weight(1f)
                .heightIn(max = 130.dp)
                .padding(end = 8.dp)
        )

        if (isEdit) {
            IconButton(onClick = onClearText) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cancel Edit",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        IconButton(
            onClick = {
                onSend()
                onClearText()
            },
            enabled = textField.text.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                tint = if (textField.text.isNotBlank()) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    Color.Gray
                }
            )
        }
    }
}

@Composable
fun CommentItem(
    modifier: Modifier = Modifier,
    user: User,
    comment: Comment,
    onOpenActionSheet: () -> Unit,
) {
    val createdAt = remember(comment.createdAt) {
        comment.createdAt?.let { createdAtRaw ->
            runCatching {
                OffsetDateTime.parse(createdAtRaw).format(CommentDateTimeFormatter)
            }.getOrElse {
                runCatching {
                    LocalDateTime.parse(createdAtRaw).format(CommentDateTimeFormatter)
                }.getOrDefault("")
            }
        }.orEmpty()
    }

    val name = if (comment.authorDeleted) "탈퇴한 사용자" else comment.authorName

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 7.dp)
            .background(MaterialTheme.colorScheme.surface, CommentItemShape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                shape = CommentItemShape
            )
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        AsyncImage(
            model = comment.authorAvatarUrl ?: R.drawable.animal_carnivore_cartoon_3_svgrepo_com,
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name ?: "닉네임이 없어요!",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                IconButton(
                    modifier = Modifier.size(24.dp),
                    onClick = onOpenActionSheet
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "comment action",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = createdAt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun KeyboardWatcher(onKeyboardClosed: () -> Unit) {
    val ime = WindowInsets.ime
    val isImeVisible = ime.getBottom(LocalDensity.current) > 0
    var wasVisible by remember { mutableStateOf(isImeVisible) }

    LaunchedEffect(isImeVisible) {
        if (wasVisible && !isImeVisible) {
            onKeyboardClosed()
        }
        wasVisible = isImeVisible
    }
}

@Composable
fun CommentActionBottomSheet(
    bottomSheetState: ModalBottomSheetState,
    uiState: CommentUiState,
    user: User,
    onEvent: (CommentEvent) -> Unit
) {
    ModalBottomSheet(
        state = bottomSheetState,
        onDismiss = { onEvent(CommentEvent.CloseActionSheet) }
    ) {
        Scrim()
        Sheet(
            modifier = Modifier
                .clip(RoundedCornerTop24)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .navigationBarsPadding()
        ) {
            val comment = uiState.currentComment
            val actions = remember(comment, user) {
                if (comment == null) {
                    emptyList()
                } else {
                    val isMe = user.id == comment.userId
                    val canManage = isMe || user.role == Role.ADMIN
                    if (canManage) {
                        listOf(CommentAction.Edit, CommentAction.Delete)
                    } else {
                        listOf(
                            CommentAction.ReportComment,
                            CommentAction.ReportUser,
                            CommentAction.BlockUser
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                actions.forEach { action ->
                    ActionRow(
                        title = actionTitle(action),
                        color = actionColor(action),
                        onClick = {
                            onEvent(CommentEvent.SelectAction(action, user))
                        }
                    )
                }

                ActionRow(
                    title = "닫기",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = { onEvent(CommentEvent.CloseActionSheet) }
                )
            }
        }
    }
}

@Composable
private fun ActionRow(
    title: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = color
        )
    }
}

@Composable
private fun actionColor(action: CommentAction): Color {
    return when (action) {
        CommentAction.Edit -> MaterialTheme.colorScheme.onSurface
        CommentAction.Delete -> MaterialTheme.colorScheme.error
        CommentAction.ReportComment -> MaterialTheme.colorScheme.onSurface
        CommentAction.ReportUser -> MaterialTheme.colorScheme.onSurface
        CommentAction.BlockUser -> MaterialTheme.colorScheme.error
    }
}

private fun actionTitle(action: CommentAction): String {
    return when (action) {
        CommentAction.Edit -> "댓글 수정"
        CommentAction.Delete -> "댓글 삭제"
        CommentAction.ReportComment -> "댓글 신고"
        CommentAction.ReportUser -> "사용자 신고"
        CommentAction.BlockUser -> "이 사용자 차단"
    }
}
