package kr.sjh.feature.favourite.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import kr.sjh.core.designsystem.components.BeMyPetTopAppBar
import kr.sjh.core.designsystem.components.EndlessLazyGridColumn
import kr.sjh.core.designsystem.components.RefreshIndicator
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.theme.RoundedCorner12
import kr.sjh.core.designsystem.theme.RoundedCorner18
import kr.sjh.core.designsystem.theme.RoundedCornerBottom24
import kr.sjh.core.designsystem.theme.RoundedCornerTop24
import kr.sjh.core.model.adoption.Pet
import kr.sjh.core.model.adoption.displayBreedName

private val FavouriteHeaderHeight = 128.dp

@Composable
fun FavouriteRoute(
    navigateToPetDetail: (Pet) -> Unit,
    viewModel: FavouriteViewModel = hiltViewModel(),
) {
    val pets by viewModel.favouritePets.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    FavouriteScreen(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        pets = pets,
        isRefreshing = isRefreshing,
        onRefresh = viewModel::refresh,
        navigateToPetDetail = navigateToPetDetail
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun FavouriteScreen(
    modifier: Modifier = Modifier,
    pets: List<Pet>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    navigateToPetDetail: (Pet) -> Unit
) {
    val state = rememberPullToRefreshState()
    val listContentPadding = PaddingValues(
        top = FavouriteHeaderHeight + 12.dp,
        bottom = 20.dp,
        start = 16.dp,
        end = 16.dp
    )

    Box(modifier = modifier) {
        PullToRefreshBox(
            state = state,
            modifier = Modifier.fillMaxSize(),
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            indicator = {
                RefreshIndicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = FavouriteHeaderHeight + 8.dp)
                        .size(50.dp),
                    state = state,
                    isRefreshing = isRefreshing
                )
            }
        ) {
            when {
                isRefreshing -> {
                    FavouriteSkeletonGrid(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = listContentPadding
                    )
                }

                pets.isEmpty() -> {
                    EmptyFavouriteState(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 28.dp)
                    )
                }

                else -> {
                    EndlessLazyGridColumn(
                        userScrollEnabled = true,
                        items = pets,
                        itemKey = { item -> item.desertionNo ?: item.hashCode() },
                        contentPadding = listContentPadding,
                        loadMore = { }
                    ) { pet ->
                        FavouritePetCard(
                            modifier = Modifier
                                .fillMaxSize(),
                            pet = pet,
                            onOpenDetail = { navigateToPetDetail(pet) }
                        )
                    }
                }
            }
        }

        BeMyPetTopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(1f)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerBottom24)
                .clip(RoundedCornerBottom24),
            title = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "관심 목록",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "저장한 친구들",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        )
    }
}

@Composable
private fun FavouriteSkeletonGrid(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
) {
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(2),
        userScrollEnabled = false,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(8) {
            FavouriteSkeletonCard(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun rememberSkeletonShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "favourite_skeleton_shimmer")
    val translate by transition.animateFloat(
        initialValue = -400f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "favourite_skeleton_translate"
    )

    val base = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
    val highlight = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
    return Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(translate - 260f, translate - 260f),
        end = Offset(translate, translate)
    )
}

@Composable
private fun FavouriteSkeletonCard(modifier: Modifier = Modifier) {
    val shimmerBrush = rememberSkeletonShimmerBrush()
    Card(
        modifier = modifier,
        shape = RoundedCorner18,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .alpha(0.95f)
                    .background(shimmerBrush, RoundedCorner12)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(16.dp)
                    .background(shimmerBrush, RoundedCorner12)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .height(14.dp)
                    .background(shimmerBrush, RoundedCorner12)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(18.dp)
                        .background(shimmerBrush, RoundedCorner12)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(18.dp)
                        .background(shimmerBrush, RoundedCorner12)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun EmptyFavouriteState(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCorner18,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "아직 저장한 친구가 없어요",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "입양 탭에서 마음에 드는 친구를 저장해보세요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FavouritePetCard(
    modifier: Modifier = Modifier,
    pet: Pet,
    onOpenDetail: () -> Unit,
) {
    val context = LocalContext.current
    val imageUrls = remember(pet) { buildFavouriteImageUrls(pet) }
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { imageUrls.size.coerceAtLeast(1) }
    )

    LaunchedEffect(pagerState.currentPage, imageUrls) {
        if (imageUrls.isEmpty()) return@LaunchedEffect

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

    Card(
        modifier = modifier,
        shape = RoundedCorner18,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            if (imageUrls.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(168.dp)
                        .clip(RoundedCornerTop24),
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
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(168.dp)
                        .clip(RoundedCornerTop24),
                    userScrollEnabled = imageUrls.size > 1,
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
                        modifier = Modifier.fillMaxSize(),
                        model = imageRequest,
                        contentDescription = "favourite_pet_image_${page + 1}",
                        contentScale = ContentScale.Crop
                    )
                }
            }

            if (imageUrls.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenDetail)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = pet.displayBreedName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = pet.happenPlace?.ifBlank { "발견 장소 정보 없음" } ?: "발견 장소 정보 없음",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FavouriteMetaChip(text = "성별 ${pet.sexCdToText}")
                    FavouriteMetaChip(text = pet.processState?.ifBlank { "상태 미상" } ?: "상태 미상")
                }
                Text(
                    text = pet.noticeNo?.let { "공고번호 $it" } ?: "공고번호 미확인",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun buildFavouriteImageUrls(pet: Pet): List<String> =
    pet.imageUrls
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .ifEmpty {
            listOfNotNull(pet.thumbnailImageUrl?.trim()?.takeIf { it.isNotBlank() })
        }

@Composable
private fun FavouriteMetaChip(text: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCorner12)
            .padding(horizontal = 8.dp, vertical = 4.dp)
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
