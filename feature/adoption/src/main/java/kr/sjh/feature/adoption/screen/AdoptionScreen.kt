package kr.sjh.feature.adoption.screen

import FilterComponent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.composables.core.ModalBottomSheetState
import com.composables.core.SheetDetent
import com.composables.core.rememberModalBottomSheetState
import kotlinx.coroutines.launch
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.BeMyPetTopAppBar
import kr.sjh.core.designsystem.components.EndlessLazyGridColumn
import kr.sjh.core.designsystem.components.LoadingComponent
import kr.sjh.core.designsystem.components.RefreshIndicator
import kr.sjh.core.designsystem.theme.ExpandedAppBarHeight
import kr.sjh.core.designsystem.theme.RoundedCorner12
import kr.sjh.core.designsystem.theme.RoundedCorner18
import kr.sjh.core.designsystem.theme.RoundedCornerBottom24
import kr.sjh.core.designsystem.theme.RoundedCornerTop24
import kr.sjh.core.model.adoption.Pet
import kr.sjh.feature.adoption.screen.filter.FilterCategoryList
import kr.sjh.feature.adoption.screen.filter.FilterViewModel
import kr.sjh.feature.adoption.state.AdoptionEvent
import kr.sjh.feature.adoption.state.AdoptionUiState
import kr.sjh.feature.adoption.state.FilterEvent
import kr.sjh.feature.adoption.state.FilterUiState
import kr.sjh.feature.adoption.state.SideEffect
import kotlin.math.roundToInt

private val AppBarScrollableHeight = 86.dp

@Composable
fun AdoptionRoute(
    modifier: Modifier = Modifier,
    viewModel: AdoptionViewModel = hiltViewModel(),
    filterViewModel: FilterViewModel = hiltViewModel(),
    navigateToPetDetail: (Pet) -> Unit
) {
    val adoptionUiState by viewModel.adoptionUiState.collectAsStateWithLifecycle()

    val filterUiState by filterViewModel.filterUiState.collectAsStateWithLifecycle()
    val latestFilterUiState by rememberUpdatedState(filterUiState)

    val peek = remember {
        SheetDetent("peek", calculateDetentHeight = { containerHeight, _ ->
            containerHeight
        })
    }

    val bottomSheetState = rememberModalBottomSheetState(
        initialDetent = SheetDetent.Hidden, listOf(SheetDetent.Hidden, peek)
    )

    val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = 0
    )

    LaunchedEffect(filterViewModel, bottomSheetState, peek) {
        filterViewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                SideEffect.HideBottomSheet -> {
                    bottomSheetState.currentDetent = SheetDetent.Hidden
                }

                SideEffect.ShowBottomSheet -> {
                    bottomSheetState.currentDetent = peek
                }

                SideEffect.FetchPets -> {
                    viewModel.onEvent(
                        AdoptionEvent.Refresh(
                            latestFilterUiState.toPetRequest()
                        )
                    )
                }
            }
        }
    }

    AdoptionScreen(
        modifier = modifier,
        adoptionUiState = adoptionUiState,
        filterUiState = filterUiState,
        sheetState = bottomSheetState,
        gridState = gridState,
        navigateToPetDetail = navigateToPetDetail,
        onEvent = viewModel::onEvent,
        onFilterEvent = filterViewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdoptionScreen(
    modifier: Modifier = Modifier,
    adoptionUiState: AdoptionUiState,
    filterUiState: FilterUiState,
    sheetState: ModalBottomSheetState,
    gridState: LazyGridState,
    navigateToPetDetail: (Pet) -> Unit,
    onEvent: (AdoptionEvent) -> Unit,
    onFilterEvent: (FilterEvent) -> Unit
) {
    val density = LocalDensity.current
    val appBarHeight = ExpandedAppBarHeight
    val scrollableHeightPx = with(density) { AppBarScrollableHeight.roundToPx().toFloat() }
    var appbarOffsetHeightPx by rememberSaveable { mutableFloatStateOf(0f) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                appbarOffsetHeightPx += available.y
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset, available: Offset, source: NestedScrollSource
            ): Offset {
                appbarOffsetHeightPx -= available.y
                return Offset.Zero
            }
        }
    }

    val state = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier.then(Modifier.nestedScroll(nestedScrollConnection))
    ) {
        PullToRefreshBox(
            state = state,
            modifier = Modifier.fillMaxSize(),
            isRefreshing = adoptionUiState.isRefreshing,
            onRefresh = {
                onEvent(AdoptionEvent.Refresh(filterUiState.toPetRequest().copy(pageNo = 1)))
                appbarOffsetHeightPx = 0f
            },
            indicator = {
                RefreshIndicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = appBarHeight - 24.dp)
                        .size(50.dp),
                    state = state,
                    isRefreshing = adoptionUiState.isRefreshing
                )
            }
        ) {
            if (!adoptionUiState.isRefreshing && adoptionUiState.pets.isEmpty()) {
                EmptyAdoptionState(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 28.dp)
                )
            }
            EndlessLazyGridColumn(
                modifier = Modifier.fillMaxSize(),
                gridState = gridState,
                userScrollEnabled = !adoptionUiState.isRefreshing,
                items = adoptionUiState.pets,
                isLoadMore = adoptionUiState.isMore,
                contentPadding = PaddingValues(
                    top = appBarHeight + 12.dp,
                    bottom = 16.dp,
                    start = 12.dp,
                    end = 12.dp
                ),
                itemKey = { item -> "${item.desertionNo}" },
                loadMore = {
                    if (!adoptionUiState.isMore) {
                        onEvent(
                            AdoptionEvent.LoadMore(
                                filterUiState.toPetRequest()
                            )
                        )
                    }
                },
            ) { item ->
                PetCard(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(enabled = !adoptionUiState.isRefreshing) {
                            navigateToPetDetail(item)
                        },
                    pet = item
                )
            }
        }

        BeMyPetTopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .offset {
                    IntOffset(
                        x = 0,
                        y = appbarOffsetHeightPx
                            .coerceIn(-scrollableHeightPx, 0f)
                            .roundToInt()
                    )
                }
                .shadow(4.dp, RoundedCornerBottom24)
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerBottom24
                )
                .clip(RoundedCornerBottom24),
            title = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "동네 보호소 소식",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(R.string.adoption),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "필터로 조건을 빠르게 좁혀보세요",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            },
            content = {
                FilterCategoryList(
                    categories = filterUiState.categoryList,
                    height = appBarHeight - AppBarScrollableHeight,
                    onFilterEvent = { event ->
                        if (event is FilterEvent.Reset) {
                            coroutineScope.launch {
                                appbarOffsetHeightPx = 0f
                                gridState.scrollToItem(0)
                            }
                        }
                        onFilterEvent(event)
                    }
                )
            }
        )

        FilterComponent(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerTop24)
                .background(MaterialTheme.colorScheme.background)
                .navigationBarsPadding(),
            filterUiState = filterUiState,
            sheetState = sheetState,
            onFilterEvent = { event ->
                when (event) {
                    is FilterEvent.ConfirmLocation,
                    is FilterEvent.ConfirmUpKind,
                    is FilterEvent.ConfirmNeuter,
                    is FilterEvent.ConfirmDateRange -> {
                        coroutineScope.launch {
                            appbarOffsetHeightPx = 0f
                            gridState.scrollToItem(0)
                        }
                        onFilterEvent(event)
                    }

                    else -> {
                        onFilterEvent(event)
                    }
                }
            }
        )
    }
}

@Composable
private fun EmptyAdoptionState(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCorner18,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "조건에 맞는 친구를 아직 찾지 못했어요",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "필터를 조정하면 더 많은 공고를 확인할 수 있어요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PetCard(modifier: Modifier = Modifier, pet: Pet) {
    Card(
        modifier = modifier,
        shape = RoundedCorner18,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
            ) {
                SubcomposeAsyncImage(
                    model = pet.thumbnailImageUrl,
                    contentDescription = "Pet",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        LoadingComponent()
                    }
                )
                if (pet.isNotice) {
                    Notice(
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.TopStart)
                    )
                }
            }
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = pet.kindName?.ifBlank { "품종 정보 없음" } ?: "품종 정보 없음",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
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
                    PetMetaChip(text = "성별 ${pet.sexCdToText}")
                    PetMetaChip(text = pet.processState?.ifBlank { "상태 미상" } ?: "상태 미상")
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

@Composable
private fun PetMetaChip(text: String) {
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

@Composable
private fun Notice(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.error, RoundedCorner12)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "공고중",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onError
        )
    }
}
