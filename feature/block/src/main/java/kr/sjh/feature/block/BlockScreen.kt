package kr.sjh.feature.block

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.BeMyPetTopAppBar
import kr.sjh.core.designsystem.components.LoadingComponent
import kr.sjh.core.designsystem.components.Title
import kr.sjh.core.designsystem.theme.RoundedCorner12
import kr.sjh.core.designsystem.theme.RoundedCorner18
import kr.sjh.core.designsystem.theme.RoundedCornerBottom24
import kr.sjh.feature.navigation.BlockEvent

@Composable
fun BlockRoute(viewModel: BlockViewModel = hiltViewModel(), onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BlockScreen(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        uiState = uiState,
        onBack = onBack,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun BlockScreen(
    modifier: Modifier = Modifier,
    uiState: BlockUiState,
    onBack: () -> Unit = {},
    onEvent: (BlockEvent) -> Unit,
) {
    var isDeleteUser by remember { mutableStateOf(false) }

    if (isDeleteUser) {
        AlertDialog(
            onDismissRequest = { isDeleteUser = false },
            title = { Text("차단 해제") },
            text = { Text("선택한 사용자의 차단을 해제하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    onEvent(BlockEvent.DeleteBlockUser)
                    isDeleteUser = false
                }) {
                    Text("해제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { isDeleteUser = false }) {
                    Text("취소", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        )
    }

    Column(modifier = modifier) {
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
                    title = "차단목록",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        )

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingComponent()
            }
            return@Column
        }

        if (uiState.blockUsers.isEmpty()) {
            EmptyBlockState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = uiState.blockUsers, key = { it.blockedUser }) { user ->
                BlockUserCard(
                    profileImageUrl = user.blockedAvatarUrl,
                    userName = user.blockedName ?: "이름없음",
                    onUnblockClick = {
                        isDeleteUser = true
                        onEvent(BlockEvent.SelectedBlockedUser(user))
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyBlockState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCorner18,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "차단한 사용자가 없습니다.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "댓글 화면에서 문제가 되는 사용자를 차단할 수 있어요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BlockUserCard(
    profileImageUrl: String?,
    userName: String,
    onUnblockClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCorner18,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = profileImageUrl,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.size(12.dp))

            Text(
                modifier = Modifier.weight(1f),
                text = userName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Button(
                onClick = onUnblockClick,
                shape = RoundedCorner12,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = "차단 해제",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Unspecified
                )
            }
        }
    }
}
