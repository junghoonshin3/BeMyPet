package kr.sjh.feature.comments

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlinx.serialization.json.jsonPrimitive
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.BeMyPetTopAppBar
import kr.sjh.core.designsystem.components.LoadingComponent
import kr.sjh.core.designsystem.components.Title
import kr.sjh.core.model.Comment
import kr.sjh.core.model.ReportType
import kr.sjh.core.model.SessionState
import kr.sjh.core.model.User
import kr.sjh.feature.comments.navigation.CommentEvent
import kr.sjh.feature.comments.navigation.CommentSideEffect
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun CommentRoute(
    session: SessionState,
    onBack: () -> Unit,
    navigateToReport: (ReportType, Comment, User) -> Unit,
    commentViewModel: CommentViewModel = hiltViewModel()
) {
    val uiState by commentViewModel.uiState.collectAsStateWithLifecycle()
    val peek = SheetDetent("peek", calculateDetentHeight = { containerHeight, sheetHeight ->
        containerHeight * 0.3f
    })

    val bottomSheetState =
        rememberModalBottomSheetState(SheetDetent.Hidden, listOf(peek, SheetDetent.Hidden))

    val focusManager = LocalFocusManager.current

    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        commentViewModel.sideEffect.collect { event ->
            Log.d("Sjh", "${event}")
            when (event) {
                CommentSideEffect.HideBottomSheet -> {
                    bottomSheetState.animateTo(SheetDetent.Hidden)
                }

                CommentSideEffect.ShowBottomSheet -> {
                    bottomSheetState.animateTo(peek)
                }

                CommentSideEffect.HideKeyboard -> {
                    keyboardController?.hide()
                }

                CommentSideEffect.ShowKeyboard -> {
                    keyboardController?.show()
                }

                is CommentSideEffect.NavigateToReport -> {
                    bottomSheetState.animateTo(SheetDetent.Hidden)
                    navigateToReport(event.reportType, event.comment, event.user)

                }
            }
        }
    }

    when (session) {
        is SessionState.Authenticated -> {
            session.user?.let { user ->
                CommentScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary)
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
        }

        SessionState.Initializing -> {}
        is SessionState.NoAuthenticated -> {
            Log.d("CommentRoute", "NoAuthenticated")
        }

        SessionState.RefreshFailure -> {}
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
        listState.scrollToItem(0)
    }

    if (uiState.isStartEditing) {
        KeyboardWatcher {
            Log.d("sjh", "keyboard 닫힘")
            onEvent(CommentEvent.OnKeyboardClosedDuringEdit)
        }
    }


    if (uiState.isEditDialogVisible) {
        AlertDialog(onDismissRequest = { },
            title = { Text("편집 중단") },
            text = { Text("편집을 중단하시겠어요?") },
            confirmButton = {
                TextButton(onClick = {
                    onEvent(CommentEvent.CancelEditing)
                }) {
                    Text("예", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    // 다이얼로그만 닫고 유지
                    onEvent(CommentEvent.StayEditing)
                }) {
                    Text("아니오", color = MaterialTheme.colorScheme.onPrimary)
                }
            })
    }

    if (uiState.isDeleteDialogVisible) {
        AlertDialog(onDismissRequest = { },
            title = { Text("삭제") },
            text = { Text("정말로 삭제하시겠어요?") },
            confirmButton = {
                TextButton(onClick = {
                    onEvent(
                        CommentEvent.Delete
                    )
                }) {
                    Text("예", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    // 다이얼로그만 닫고 유지
                    onEvent(CommentEvent.DismissDeleteDialog)
                }) {
                    Text("아니오", color = MaterialTheme.colorScheme.onPrimary)
                }
            })
    }
    ReportBottomSheet(bottomSheetState, uiState, user, onEvent)
    Column(modifier = modifier) {
        BeMyPetTopAppBar(modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary),
            title = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.baseline_arrow_back_24),
                        contentDescription = "back",
                    )
                }
                Title(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    title = "댓글",
                    style = MaterialTheme.typography.headlineSmall
                )
            })

        if (uiState.loading) {
            LoadingComponent()
            return@Column
        }
        if (uiState.comments.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "댓글이 없어요.\n 궁금한 점이나 후기를 작성해보세요!",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                reverseLayout = true,
                verticalArrangement = Arrangement.Top
            ) {
                items(uiState.comments, key = { it.id }) { comment ->
                    CommentItem(comment = comment,
                        isMe = user.id == comment.userId,
                        modifier = Modifier.fillMaxWidth(),
                        onDelete = {
                            onEvent(CommentEvent.ShowDeleteDialog(comment))
                        },
                        onReport = {
                            onEvent(CommentEvent.Report(comment, user))
                        },
                        onEdit = {
                            onEvent(CommentEvent.StartEditing(comment))
                        })
                }
            }
        }


        CommentInput(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 5.dp, end = 5.dp)
            .background(Color.LightGray, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp),
            isEdit = uiState.isStartEditing,
            textField = uiState.textFieldValue,
            onTextChange = {
                onEvent(CommentEvent.OnChangeText(it))
            },
            onClearText = {
                onEvent(CommentEvent.OnClearText)
            },
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
                                rawUserMetaData = user.rawUserMetaData
                            )
                        )
                    }
                )
            })
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
    Row(
        modifier = modifier, verticalAlignment = Alignment.CenterVertically
    ) {
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
            textStyle = TextStyle(color = Color.Black),
            modifier = Modifier
                .weight(1f)
                .heightIn(max = 130.dp)
                .padding(end = 8.dp)
        )

        if (isEdit) {
            IconButton(onClick = onClearText) {
                Icon(Icons.Default.Close, contentDescription = "Cancel Edit", tint = Color.Red)
            }
        }

        IconButton(
            onClick = {
                onSend()
                onClearText()
            }, enabled = textField.text.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                tint = if (textField.text.isNotBlank()) MaterialTheme.colorScheme.onBackground else Color.Gray
            )
        }
    }
}

@Composable
fun CommentItem(
    modifier: Modifier = Modifier,
    isMe: Boolean,
    comment: Comment,
    onDelete: (String) -> Unit,
    onReport: (Comment) -> Unit,
    onEdit: (Comment) -> Unit
) {
    val createAt = LocalDateTime.parse(comment.createdAt)
        .format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분"))
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(12.dp), verticalAlignment = Alignment.Top
    ) {
        val profile = comment.rawUserMetaData?.get("avatar_url")?.jsonPrimitive?.content
        val name = comment.rawUserMetaData?.get("name")?.jsonPrimitive?.content
        // 프로필 사진
        AsyncImage(
            model = profile ?: R.drawable.animal_carnivore_cartoon_3_svgrepo_com,
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            // 사용자 이름과 작성 시간
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = name ?: "닉네임이 없어요!",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                if (isMe) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(modifier = Modifier.size(25.dp),
                        onClick = { onDelete(comment.id.toString()) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Comment",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(modifier = Modifier.size(25.dp), onClick = { onEdit(comment) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Comment",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                if (!isMe) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(modifier = Modifier.size(25.dp), onClick = { onReport(comment) }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.siren_rounded_svgrepo_com),
                            contentDescription = "Siren Comment",
                            tint = Color.Red
                        )
                    }
                }

            }

            Spacer(modifier = Modifier.height(4.dp))

            // 댓글 내용
            Text(
                text = comment.content, style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            // 작성 시간
            Text(
                text = createAt ?: "",
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
            onKeyboardClosed() // 키보드 닫힘 감지!
        }
        wasVisible = isImeVisible
    }
}

@Composable
fun ReportBottomSheet(
    bottomSheetState: ModalBottomSheetState,
    uiState: CommentUiState,
    user: User,
    onEvent: (CommentEvent) -> Unit
) {
    ModalBottomSheet(state = bottomSheetState, onDismiss = {}) {
        Scrim()
        Sheet(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
                .navigationBarsPadding()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                items(ReportType.entries.toList()) { type ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clickable {
                                uiState.currentComment?.let {
                                    onEvent(
                                        CommentEvent.SelectReportType(
                                            type, uiState.currentComment, user
                                        )
                                    )
                                }
                            }, contentAlignment = Alignment.Center
                    ) {
                        val style = when (type) {
                            ReportType.Comment -> MaterialTheme.typography.titleMedium.copy(
                                color = Color.Blue,
                            )

                            ReportType.User -> MaterialTheme.typography.titleMedium.copy(
                                color = Color.Red,
                            )
                        }
                        Text(
                            text = "${type.title} 신고", style = style
                        )
                    }
                }
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clickable {
                                val blockedUserId = uiState.currentComment?.userId

                                blockedUserId?.let { blocked ->
                                    onEvent(
                                        if (uiState.isBlockedUser) {
                                            CommentEvent.UnBlock(
                                                user.id, blocked
                                            )
                                        } else {
                                            CommentEvent.Block(
                                                user.id, blocked
                                            )
                                        }
                                    )
                                }
                            }, contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.isBlockedUser) "이 사용자 차단해제" else "이 사용자 차단",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.DarkGray,
                            )
                        )
                    }
                }
            }
        }
    }
}