package kr.sjh.feature.adoption_detail.screen

import android.os.Bundle
import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kr.sjh.core.common.ads.AdMobBanner
import kr.sjh.core.common.share.sharePet
import kr.sjh.core.common.snackbar.SnackBarManager
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.BeMyPetBackAppBar
import kr.sjh.core.designsystem.components.LoadingComponent
import kr.sjh.core.designsystem.theme.RoundedCorner12
import kr.sjh.core.designsystem.theme.RoundedCorner18
import kr.sjh.core.designsystem.theme.RoundedCornerBottom24
import kr.sjh.core.model.SessionState
import kr.sjh.core.model.adoption.Pet
import kr.sjh.core.model.adoption.displayBreedName
import kr.sjh.data.repository.CompareToggleResult
import kr.sjh.feature.adoption_detail.state.AdoptionDetailEvent
import kr.sjh.feature.adoption_detail.state.DetailUiState
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
fun PetDetailRoute(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    session: SessionState,
    viewModel: PetDetailViewModel = hiltViewModel(),
    onNavigateToComments: (String, String) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToCompareBoard: () -> Unit
) {
    val location by viewModel.location.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val commentCount by viewModel.commentCount.collectAsStateWithLifecycle()
    val comparedCount by viewModel.comparedCount.collectAsStateWithLifecycle()
    val isCompared by viewModel.isCompared.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshCommentCount()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val context = LocalContext.current
    val firebaseAnalytics = remember(context) { FirebaseAnalytics.getInstance(context) }
    val currentSession = session
    val commentLocked = when (currentSession) {
        is SessionState.Authenticated -> currentSession.user.isBanned
        is SessionState.Banned -> true
        else -> false
    }

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is DetailUiState.Success) {
            firebaseAnalytics.logEvent("pet_detail_view", Bundle().apply {
                putString("notice_no", state.pet.noticeNo.orEmpty())
                putString("up_kind", state.pet.upKindCode.orEmpty())
            })
        }
    }

    LaunchedEffect(viewModel, firebaseAnalytics) {
        viewModel.compareToggleEvent.collect { event ->
            firebaseAnalytics.logEvent("compare_toggle", Bundle().apply {
                putString("action", event.action)
                putString(
                    "result",
                    when (event.result) {
                        CompareToggleResult.Added -> "added"
                        CompareToggleResult.Removed -> "removed"
                        CompareToggleResult.LimitExceeded -> "limit_exceeded"
                    }
                )
                putInt("selected_count", event.selectedCount)
            })
        }
    }

    PetDetailScreen(
        modifier = modifier,
        uiState = uiState,
        isFavorite = isFavorite,
        isCompared = isCompared,
        isCommentLocked = commentLocked,
        onBack = onBack,
        state = location,
        commentCount = commentCount,
        comparedCount = comparedCount,
        onShare = { pet -> context.sharePet(pet) },
        onFavorite = { like ->
            viewModel.onEvent(AdoptionDetailEvent.OnFavorite(like))
        },
        onToggleCompare = {
            viewModel.onEvent(AdoptionDetailEvent.ToggleCompare)
        },
        onOpenCompareBoard = {
            firebaseAnalytics.logEvent("compare_board_open", Bundle().apply {
                putInt("selected_count", comparedCount)
            })
            onNavigateToCompareBoard()
        },
        onNavigateToComments = { noticeNo ->
            when (val state = session) {
                is SessionState.Authenticated -> {
                    if (state.user.isBanned) {
                        SnackBarManager.showMessage(
                            "${formatBannedUntil(state.user.bannedUntil)}까지 댓글 기능이 제한되어 있어요."
                        )
                    } else {
                        onNavigateToComments(noticeNo, state.user.id)
                    }
                }

                is SessionState.NoAuthenticated -> {
                    SnackBarManager.showMessage("회원가입 화면으로 이동합니다.")
                    onNavigateToSignUp()
                }

                is SessionState.Banned -> {
                    SnackBarManager.showMessage(
                        "${formatBannedUntil(state.bannedUntil)}까지 댓글 기능이 제한되어 있어요."
                    )
                }

                else -> Unit
            }
        }
    )
}

@Composable
private fun PetDetailScreen(
    modifier: Modifier = Modifier,
    uiState: DetailUiState,
    isFavorite: Boolean,
    isCompared: Boolean,
    isCommentLocked: Boolean,
    commentCount: Int,
    comparedCount: Int,
    state: LocationUiState,
    onBack: () -> Unit,
    onShare: (Pet) -> Unit,
    onFavorite: (Boolean) -> Unit,
    onToggleCompare: () -> Unit,
    onOpenCompareBoard: () -> Unit,
    onNavigateToComments: (String) -> Unit
) {
    val context = LocalContext.current
    Box(modifier = modifier) {
        when (uiState) {
            is DetailUiState.Failure -> {
                uiState.e.printStackTrace()
                Log.d("PetDetailScreen", "Failure")
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "상세 정보를 불러오지 못했어요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            DetailUiState.Loading -> {
                PetDetailLoadingSkeleton(onBack = onBack)
            }

            is DetailUiState.Success -> {
                var zoomImageUrl by remember(uiState.pet.noticeNo) { mutableStateOf<String?>(null) }

                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        PetDetailHeaderBar(
                            modifier = Modifier.fillMaxWidth(),
                            title = "동물 상세",
                            isFavorite = isFavorite,
                            onBack = onBack,
                            onShare = { onShare(uiState.pet) },
                            onFavorite = { onFavorite(!isFavorite) }
                        )

                        AdMobBanner()

                        PetDetailContent(
                            pet = uiState.pet,
                            isCompared = isCompared,
                            isCommentLocked = isCommentLocked,
                            commentCount = commentCount,
                            comparedCount = comparedCount,
                            state = state,
                            onToggleCompare = onToggleCompare,
                            onOpenCompareBoard = onOpenCompareBoard,
                            onNavigateToComments = onNavigateToComments,
                            onOpenImageZoom = { imageUrl ->
                                zoomImageUrl = imageUrl
                            }
                        )
                    }

                    if (!zoomImageUrl.isNullOrBlank()) {
                        val imageRequest = remember(zoomImageUrl, context) {
                            ImageRequest.Builder(context)
                                .data(zoomImageUrl)
                                .crossfade(true)
                                .build()
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(10f)
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            PetPinedZoomRoute(
                                imageRequest = imageRequest,
                                close = { zoomImageUrl = null }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PetDetailHeaderBar(
    modifier: Modifier = Modifier,
    title: String,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onFavorite: () -> Unit,
) {
    BeMyPetBackAppBar(
        modifier = modifier,
        title = title,
        onBack = onBack,
        actions = {
            IconButton(
                modifier = Modifier.size(40.dp),
                onClick = onShare
            ) {
                Icon(
                    modifier = Modifier.size(22.dp),
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_share_24),
                    contentDescription = "share",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            IconButton(
                modifier = Modifier.size(40.dp),
                onClick = onFavorite
            ) {
                Icon(
                    modifier = Modifier.size(22.dp),
                    imageVector = ImageVector.vectorResource(
                        id = if (isFavorite) R.drawable.like_filled else R.drawable.like
                    ),
                    contentDescription = "like",
                    tint = if (isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    )
}

@Composable
private fun PetDetailLoadingSkeleton(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val shimmer = rememberDetailSkeletonBrush()

    Column(modifier = modifier.fillMaxSize()) {
        PetDetailHeaderBar(
            modifier = Modifier.fillMaxWidth(),
            title = "동물 상세",
            isFavorite = false,
            onBack = onBack,
            onShare = {},
            onFavorite = {}
        )
        AdMobBanner()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    shape = RoundedCorner18,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(248.dp)
                            .alpha(0.95f)
                            .background(shimmer, RoundedCorner18)
                    )
                }
            }
            items(3) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCorner18,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.4f)
                                .height(18.dp)
                                .background(shimmer, RoundedCorner12)
                        )
                        repeat(4) { index ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(if (index % 2 == 0) 0.9f else 0.72f)
                                    .height(14.dp)
                                    .background(shimmer, RoundedCorner12)
                            )
                        }
                    }
                }
            }
            item { Box(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun rememberDetailSkeletonBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "detail_skeleton_shimmer")
    val translate by transition.animateFloat(
        initialValue = -400f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "detail_skeleton_translate"
    )
    val base = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
    val highlight = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
    return Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = androidx.compose.ui.geometry.Offset(translate - 260f, translate - 260f),
        end = androidx.compose.ui.geometry.Offset(translate, translate)
    )
}

@Composable
private fun PetDetailContent(
    pet: Pet,
    isCompared: Boolean,
    isCommentLocked: Boolean,
    commentCount: Int,
    comparedCount: Int,
    state: LocationUiState,
    onToggleCompare: () -> Unit,
    onOpenCompareBoard: () -> Unit,
    onNavigateToComments: (String) -> Unit,
    onOpenImageZoom: (String) -> Unit,
) {
    val imageUrls = remember(pet) { buildDetailImageUrls(pet) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PetHeroImagePager(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                imageUrls = imageUrls,
                onImageClick = { imageUrl ->
                    onOpenImageZoom(imageUrl)
                }
            )
        }

        item {
            SummarySection(
                modifier = Modifier.padding(horizontal = 16.dp),
                pet = pet,
                isCompared = isCompared,
                isCommentLocked = isCommentLocked,
                commentCount = commentCount,
                comparedCount = comparedCount,
                onToggleCompare = onToggleCompare,
                onOpenCompareBoard = onOpenCompareBoard,
                onNavigateToComments = onNavigateToComments
            )
        }

        item {
            InfoSectionCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = "발견/공고 정보"
            ) {
                InfoRow(title = "공고번호", value = pet.noticeNo ?: "정보없음")
                InfoRow(title = "유기번호", value = pet.desertionNo ?: "정보없음")
                InfoRow(title = "접수일", value = pet.happenDate ?: "정보없음")
                InfoRow(title = "발견장소", value = pet.happenPlace ?: "정보없음", maxLines = 2)
                InfoRow(
                    title = "공고기간",
                    value = "${pet.noticeStartDate ?: "-"} ~ ${pet.noticeEndDate ?: "-"}",
                    maxLines = 1
                )
            }
        }

        item {
            InfoSectionCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = "보호소 정보"
            ) {
                InfoRow(title = "보호소 이름", value = pet.careName ?: "정보없음")
                InfoRow(title = "보호소 연락처", value = pet.careTel ?: "정보없음")
                InfoRow(title = "보호장소", value = pet.careAddress ?: "정보없음", maxLines = 2)
                ShelterMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCorner12),
                    mapId = pet.careTel ?: "map",
                    careNm = pet.careName ?: "보호소",
                    state = state
                )
            }
        }

        item {
            InfoSectionCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = "추가 정보"
            ) {
                InfoRow(title = "품종", value = pet.displayBreedName, maxLines = 2)
                InfoRow(title = "성별", value = pet.sexCdToText)
                InfoRow(title = "상태", value = pet.processState ?: "정보없음")
                InfoRow(title = "중성화", value = pet.neuterYnToText)
                InfoRow(title = "나이", value = pet.age ?: "정보없음")
                InfoRow(title = "체중", value = pet.weight ?: "정보없음")
                InfoRow(title = "색상", value = pet.color ?: "정보없음")
                InfoRow(title = "관할기관", value = pet.organizationName ?: "정보없음", maxLines = 2)
                InfoRow(title = "특징", value = pet.specialMark ?: "정보없음", maxLines = 3)
            }
        }

        item {
            Box(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SummarySection(
    modifier: Modifier = Modifier,
    pet: Pet,
    isCompared: Boolean,
    isCommentLocked: Boolean,
    commentCount: Int,
    comparedCount: Int,
    onToggleCompare: () -> Unit,
    onOpenCompareBoard: () -> Unit,
    onNavigateToComments: (String) -> Unit
) {
    val noticeNo = pet.noticeNo
    val canOpenComment = !noticeNo.isNullOrBlank()
    val showLockedCommentButton = canOpenComment && isCommentLocked
    val canOpenCompareBoard = comparedCount >= 2

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCorner18,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = pet.displayBreedName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = pet.happenPlace ?: "발견 장소 정보 없음",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MetaChip(text = "성별 ${pet.sexCdToText}")
                MetaChip(text = pet.processState ?: "상태 미확인")
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(vertical = 4.dp),
                enabled = canOpenComment,
                onClick = {
                    noticeNo?.let(onNavigateToComments)
                },
                shape = RoundedCorner12,
                colors = if (showLockedCommentButton) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (showLockedCommentButton) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.baseline_lock_24),
                            contentDescription = "댓글 잠금",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = if (canOpenComment) {
                            "댓글 ${commentCount}개 보기"
                        } else {
                            "댓글 정보를 불러올 수 없어요"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    onClick = onToggleCompare,
                    shape = RoundedCorner12,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompared) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                        contentColor = if (isCompared) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                ) {
                    Text(
                        text = if (isCompared) "비교 해제" else "비교 담기",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    enabled = canOpenCompareBoard,
                    onClick = onOpenCompareBoard,
                    shape = RoundedCorner12,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        text = "비교보드 보기 ($comparedCount/3)",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (!canOpenCompareBoard) {
                Text(
                    text = "비교보드는 2마리 이상 담으면 열 수 있어요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoSectionCard(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCorner18,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            content()
        }
    }
}

@Composable
private fun InfoRow(
    title: String,
    value: String,
    maxLines: Int = 1
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.width(72.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MetaChip(text: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCorner12)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PetHeroImagePager(
    modifier: Modifier = Modifier,
    imageUrls: List<String>,
    onImageClick: (String) -> Unit
) {
    if (imageUrls.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCorner18,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(248.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.fg_ic_image_placeholder),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "이미지가 없어요",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        return
    }

    val context = LocalContext.current
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { imageUrls.size }
    )

    LaunchedEffect(pagerState.currentPage, imageUrls) {
        val adjacentIndices = listOf(
            pagerState.currentPage - 1,
            pagerState.currentPage + 1
        ).filter { it in imageUrls.indices }

        adjacentIndices.forEach { index ->
            context.imageLoader.enqueue(
                ImageRequest.Builder(context)
                    .data(imageUrls[index])
                    .crossfade(true)
                    .build()
            )
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCorner18,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(248.dp),
                beyondViewportPageCount = 1
            ) { page ->
                val imageUrl = imageUrls[page]
                val imageRequest = remember(imageUrl, context) {
                    ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .build()
                }
                AsyncImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onImageClick(imageUrl) },
                    model = imageRequest,
                    contentDescription = "pet_detail_image_${page + 1}",
                    contentScale = ContentScale.Crop
                )
            }
        }

        if (imageUrls.size > 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(imageUrls.size) { index ->
                        val selected = index == pagerState.currentPage
                        Box(
                            modifier = Modifier
                                .size(if (selected) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) {
                                        MaterialTheme.colorScheme.secondary
                                    } else {
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.75f)
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
}

private fun buildDetailImageUrls(pet: Pet): List<String> {
    val distinctUrls = pet.imageUrls
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()

    return if (distinctUrls.isNotEmpty()) {
        distinctUrls
    } else {
        listOfNotNull(pet.thumbnailImageUrl?.trim()?.takeIf { it.isNotBlank() })
    }
}

private val BannedUntilFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분")

private fun formatBannedUntil(raw: String?): String {
    if (raw.isNullOrBlank()) return "알 수 없는 시각"

    return runCatching {
        OffsetDateTime.parse(raw).toLocalDateTime().format(BannedUntilFormatter)
    }.recoverCatching {
        LocalDateTime.parse(raw).format(BannedUntilFormatter)
    }.getOrDefault(raw)
}

@Composable
private fun ShelterMap(
    modifier: Modifier = Modifier,
    mapId: String,
    careNm: String,
    state: LocationUiState
) {
    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            is LocationUiState.Failure -> {
                Text(text = "지도를 읽어오는데 실패했습니다")
            }

            LocationUiState.Loading -> {
                LoadingComponent()
            }

            is LocationUiState.Success -> {
                val location = state.location
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(
                        LatLng(location.latitude, location.longitude),
                        17f
                    )
                }

                val markerState = rememberMarkerState(
                    key = mapId,
                    position = LatLng(location.latitude, location.longitude)
                )
                val mapProperties = remember {
                    MapProperties(maxZoomPreference = 19f, minZoomPreference = 5f)
                }
                val mapUiSettings = remember {
                    MapUiSettings(
                        compassEnabled = false,
                        indoorLevelPickerEnabled = false,
                        mapToolbarEnabled = false,
                        myLocationButtonEnabled = false,
                        rotationGesturesEnabled = false,
                        scrollGesturesEnabled = false,
                        scrollGesturesEnabledDuringRotateOrZoom = false,
                        tiltGesturesEnabled = false,
                        zoomControlsEnabled = true,
                        zoomGesturesEnabled = true,
                    )
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    properties = mapProperties,
                    uiSettings = mapUiSettings,
                    cameraPositionState = cameraPositionState,
                ) {
                    Marker(
                        state = markerState,
                        title = careNm
                    )
                }
            }
        }
    }
}
