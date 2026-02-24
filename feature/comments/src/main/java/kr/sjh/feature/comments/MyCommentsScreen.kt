package kr.sjh.feature.comments

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kr.sjh.core.designsystem.components.BeMyPetBackAppBar
import kr.sjh.core.designsystem.components.BeMyPetConfirmDialog
import kr.sjh.core.designsystem.components.BeMyPetDialogActionStyle
import kr.sjh.core.designsystem.components.LoadingComponent
import kr.sjh.core.designsystem.theme.RoundedCorner12
import kr.sjh.core.designsystem.theme.RoundedCorner18
import kr.sjh.core.model.Comment

private val MyCommentDateFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분")

@Composable
fun MyCommentsRoute(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onNavigateToAdoption: () -> Unit,
    onNavigateToComments: (String, String) -> Unit,
    viewModel: MyCommentsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MyCommentsScreen(
        modifier = modifier,
        uiState = uiState,
        onBack = onBack,
        onNavigateToAdoption = onNavigateToAdoption,
        onOpenThread = { noticeNo ->
            onNavigateToComments(noticeNo, viewModel.userId)
        },
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun MyCommentsScreen(
    modifier: Modifier = Modifier,
    uiState: MyCommentsUiState,
    onBack: () -> Unit,
    onNavigateToAdoption: () -> Unit,
    onOpenThread: (String) -> Unit,
    onEvent: (MyCommentsEvent) -> Unit,
) {
    val pendingDeleteComment = uiState.pendingDeleteComment
    if (pendingDeleteComment != null) {
        BeMyPetConfirmDialog(
            onDismissRequest = { onEvent(MyCommentsEvent.DismissDeleteDialog) },
            title = "댓글 삭제",
            message = "선택한 댓글을 삭제할까요?",
            confirmText = "삭제",
            dismissText = "취소",
            confirmActionStyle = BeMyPetDialogActionStyle.Destructive,
            onConfirm = {
                onEvent(MyCommentsEvent.ConfirmDelete(pendingDeleteComment.id))
            },
            onDismiss = { onEvent(MyCommentsEvent.DismissDeleteDialog) }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BeMyPetBackAppBar(
            modifier = Modifier.fillMaxWidth(),
            title = "내가 쓴 댓글",
            onBack = onBack,
        )

        when {
            uiState.loading -> {
                LoadingComponent()
            }

            uiState.comments.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "아직 작성한 댓글이 없어요.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "입양 목록에서 궁금한 점을 댓글로 남겨보세요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onNavigateToAdoption,
                        shape = RoundedCorner12,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Text(
                            text = "입양 목록 보러가기",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp,
                        vertical = 14.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.comments, key = { it.id }) { comment ->
                        MyCommentListItem(
                            comment = comment,
                            onOpenThread = onOpenThread,
                            onDelete = { onEvent(MyCommentsEvent.RequestDelete(comment)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MyCommentListItem(
    comment: Comment,
    onOpenThread: (String) -> Unit,
    onDelete: () -> Unit,
) {
    val noticeNo = comment.noticeNo.orEmpty().trim()
    val canOpenThread = noticeNo.isNotBlank()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCorner18,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (canOpenThread) "공고번호: $noticeNo" else "공고번호 정보없음",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = formatMyCommentDateTime(comment.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = canOpenThread,
                    onClick = {
                        if (canOpenThread) {
                            onOpenThread(noticeNo)
                        }
                    },
                    shape = RoundedCorner12,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        text = "댓글 스레드 보기",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .clickable(onClick = onDelete),
                    text = "삭제",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun formatMyCommentDateTime(raw: String?): String {
    if (raw.isNullOrBlank()) return "작성 시각 정보없음"

    return runCatching {
        OffsetDateTime.parse(raw).format(MyCommentDateFormatter)
    }.recoverCatching {
        LocalDateTime.parse(raw).format(MyCommentDateFormatter)
    }.getOrDefault(raw)
}
