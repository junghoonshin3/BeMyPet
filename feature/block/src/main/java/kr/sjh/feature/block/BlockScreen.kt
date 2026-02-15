package kr.sjh.feature.block

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
//    onDeleteUser: () -> Unit,
//    selectedBlockUser: (BlockUser) -> Unit
) {
    var isDeleteUser by remember { mutableStateOf(false) }

    if (isDeleteUser) {
        AlertDialog(onDismissRequest = { },
            title = { Text("차단목록") },
            text = { Text("정말 차단해제 하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    onEvent(BlockEvent.DeleteBlockUser)
                    isDeleteUser = false
                }) {
                    Text("예", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    // 다이얼로그만 닫고 유지
                    isDeleteUser = false
                }) {
                    Text("아니오", color = MaterialTheme.colorScheme.onPrimary)
                }
            })
    }

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
                    title = "차단목록",
                    style = MaterialTheme.typography.headlineSmall
                )
            })

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingComponent()
            }
            return@Column
        }

        if (uiState.blockUsers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "차단한 사용자가 없습니다.")
            }
            return@Column
        }

        LazyColumn(modifier.fillMaxSize()) {
            items(uiState.blockUsers) { user ->
                val profileImageUrl = user.blockedAvatarUrl ?: ""
                val userName = user.blockedName ?: "이름없음"

                UserProfileCard(profileImageUrl = profileImageUrl,
                    userName = userName,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .clickable {
                            isDeleteUser = true
                            onEvent(BlockEvent.SelectedBlockedUser(user))
                        })
            }
        }
    }
}

@Composable
fun UserProfileCard(
    profileImageUrl: String, userName: String, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = profileImageUrl,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = userName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
