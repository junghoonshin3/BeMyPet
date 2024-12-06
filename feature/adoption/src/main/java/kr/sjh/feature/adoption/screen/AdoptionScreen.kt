package kr.sjh.feature.adoption.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.Size
import com.composables.core.ModalBottomSheet
import com.composables.core.Scrim
import com.composables.core.Sheet
import com.composables.core.SheetDetent
import com.composables.core.SheetDetent.Companion.Hidden
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.CustomPullToRefreshBox
import kr.sjh.core.designsystem.components.EndlessLazyGridColumn
import kr.sjh.core.designsystem.components.LoadingComponent
import kr.sjh.core.designsystem.components.RefreshIndicator
import kr.sjh.core.designsystem.components.RoundedCornerButton
import kr.sjh.core.designsystem.components.TextLine
import kr.sjh.core.model.adoption.Pet
import kr.sjh.feature.adoption.screen.filter.CategoryType
import kr.sjh.feature.adoption.screen.filter.DateRangePickerModal
import kr.sjh.feature.adoption.screen.filter.FilterContent
import kr.sjh.feature.adoption.state.AdoptionEvent
import kr.sjh.feature.adoption.state.AdoptionFilterState
import kr.sjh.feature.adoption.state.AdoptionUiState

@Composable
fun AdoptionRoute(
    viewModel: AdoptionViewModel = hiltViewModel(), navigateToPetDetail: (Pet) -> Unit
) {
    val adoptionUiState by viewModel.adoptionUiState.collectAsStateWithLifecycle()

    val filterState by viewModel.adoptionFilterState.collectAsStateWithLifecycle()

    AdoptionScreen(
        adoptionUiState = adoptionUiState,
        adoptionFilterState = filterState,
        navigateToAdoptionDetail = navigateToPetDetail,
        onEvent = viewModel::onEvent
    )
}

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdoptionScreen(
    adoptionUiState: AdoptionUiState,
    adoptionFilterState: AdoptionFilterState,
    navigateToAdoptionDetail: (Pet) -> Unit,
    onEvent: (AdoptionEvent) -> Unit
) {

    val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = 0
    )

    val density = LocalDensity.current

    val pullToRefreshState = rememberPullToRefreshState()

    val threshold = 80.dp

    val thresholdPx = with(density) {
        threshold.roundToPx()
    }

    // PullToRefresh 상태 - 새로고침 UI 진행 여부
    val isRefreshingDistance by remember(pullToRefreshState.distanceFraction) {
        derivedStateOf {
            pullToRefreshState.distanceFraction <= 0f
        }
    }

    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(canScroll = {
        isRefreshingDistance
    })


    //TopBar 펼치진 상태
    val isExpandedTopBar by remember(topAppBarScrollBehavior.state) {
        derivedStateOf {
            topAppBarScrollBehavior.state.heightOffset >= 0f
        }
    }

    var isDatePickerShow by remember { mutableStateOf(false) }

    val Peek = SheetDetent(identifier = "peek") { containerHeight, sheetHeight ->
        containerHeight * 0.6f
    }

    val sheetState = com.composables.core.rememberModalBottomSheetState(
        initialDetent = Hidden, detents = listOf(Hidden, Peek)
    )

    //마지막 스크롤 인덱스
    LaunchedEffect(gridState) {
        snapshotFlow {
            gridState.firstVisibleItemIndex
        }.debounce(500L).collectLatest { index ->
            onEvent(
                AdoptionEvent.SetLastScrollIndex(
                    index
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) {
        if (isDatePickerShow) {
            DateRangePickerModal(adoptionFilterState.selectedDateRange,
                onDateRangeSelected = { selectedDateRange ->
                    adoptionFilterState.selectedCategory?.apply {
                        isSelected.value = !selectedDateRange.isInitSameDate()
                        displayNm.value =
                            if (!selectedDateRange.isInitSameDate()) selectedDateRange.toString() else type.title
                    }
                    onEvent(
                        AdoptionEvent.SelectedDateRange(selectedDateRange)
                    )
                },
                onDismiss = {
                    isDatePickerShow = false
                })
        }

        TopAppBar(
            title = {
                Text(
                    text = stringResource(id = R.string.adoption),
                )
            }, scrollBehavior = topAppBarScrollBehavior
        )

        Text(
            modifier = Modifier
                .padding(end = 5.dp)
                .align(Alignment.End),
            text = "${adoptionUiState.pets.size}/${adoptionUiState.totalCount}"
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                IconButton(onClick = {
                    onEvent(
                        AdoptionEvent.InitCategory
                    )
                }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.refresh_svgrepo_com),
                        contentDescription = "reset"
                    )
                }
            }
            items(adoptionFilterState.filterList) { category ->
                RoundedCornerButton(modifier = Modifier.padding(5.dp),
                    selected = category.isSelected.value,
                    title = if (category.isSelected.value) category.displayNm.value else category.type.title,
                    onClick = {
                        when (category.type) {
                            CategoryType.DATE_RANGE -> {
                                isDatePickerShow = true
                            }

                            else -> {
                                sheetState.currentDetent = Peek
                            }
                        }
                        onEvent(
                            AdoptionEvent.SelectedCategory(
                                category
                            )
                        )
                    })
            }
        }

        CustomPullToRefreshBox(enabled = isExpandedTopBar,
            state = pullToRefreshState,
            indicator = {
                RefreshIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    state = pullToRefreshState,
                    isRefreshing = adoptionUiState.isRefreshing,
                    threshold = 80.dp
                )
            },
            isRefreshing = adoptionUiState.isRefreshing,
            onRefresh = { onEvent(AdoptionEvent.Refresh) }) {
            EndlessLazyGridColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .offset {
                        IntOffset(
                            0, (pullToRefreshState.distanceFraction * thresholdPx).toInt()
                        )
                    },
                gridState = gridState,
                userScrollEnabled = !adoptionUiState.isRefreshing,
                items = adoptionUiState.pets,
                itemKey = { item -> item.desertionNo },
                loadMore = {
                    // 현재 아이템의 갯수 < 전체 아이템의 수 && api 호출 중이 아니면 로드
                    if (adoptionUiState.pets.size < adoptionUiState.totalCount && !adoptionUiState.isMore) {
                        onEvent(AdoptionEvent.LoadMore)
                    }
                },
            ) { item ->
                Pet(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(enabled = !adoptionUiState.isRefreshing) {
                            navigateToAdoptionDetail(item)
                        }, pet = item
                )
            }
            ModalBottomSheet(state = sheetState) {
                Scrim()
                Sheet(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(Color.White)
                        .navigationBarsPadding(), enabled = false
                ) {
                    FilterContent(
                        adoptionFilterState = adoptionFilterState,
                        sheetState = sheetState,
                        onEvent = onEvent
                    )
                }
            }
        }
    }
}


@Composable
private fun Pet(modifier: Modifier = Modifier, pet: Pet) {
    val fontSize = 9.sp
    val painter = rememberAsyncImagePainter(model = pet.popfile)
    Column(
        modifier = modifier
    ) {
        SubcomposeAsyncImage(modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(10.dp)),
            model = painter.request,
            contentDescription = "Pet",
            loading = {
                LoadingComponent()
            },
            success = {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        painter = painter,
                        contentDescription = ""
                    )
                    if (pet.isNotice) {
                        Notice(
                            modifier = Modifier
                                .alpha(0.6f)
                                .background(Color.Red, RoundedCornerShape(3.dp))
                        )
                    }
                }
            })

        TextLine(
            title = "공고번호",
            content = pet.noticeNo,
            titleTextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = fontSize),
            contentTextStyle = TextStyle(fontWeight = FontWeight.Light, fontSize = fontSize)
        )
        TextLine(
            title = "발견장소",
            content = pet.happenPlace,
            titleTextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = fontSize),
            contentTextStyle = TextStyle(fontWeight = FontWeight.Light, fontSize = fontSize)
        )
        TextLine(
            title = "품종",
            content = pet.kindCd,
            titleTextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = fontSize),
            contentTextStyle = TextStyle(fontWeight = FontWeight.Light, fontSize = fontSize)
        )
        TextLine(
            title = "성별",
            content = pet.sexCdToText,
            titleTextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = fontSize),
            contentTextStyle = TextStyle(fontWeight = FontWeight.Light, fontSize = fontSize)
        )
        TextLine(
            title = "상태",
            content = pet.processState,
            titleTextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = fontSize),
            contentTextStyle = TextStyle(fontWeight = FontWeight.Light, fontSize = fontSize)
        )
    }
}

@Composable
private fun Notice(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
    ) {
        Text(modifier = Modifier.padding(3.dp), text = "공고중", fontSize = 13.sp, color = Color.White)
    }
}

