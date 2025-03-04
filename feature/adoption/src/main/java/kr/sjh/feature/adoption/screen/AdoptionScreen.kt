package kr.sjh.feature.adoption.screen

import FilterComponent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import com.composables.core.ModalBottomSheetState
import com.composables.core.SheetDetent
import com.composables.core.rememberModalBottomSheetState
import kotlinx.coroutines.launch
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.BeMyPetTopAppBar
import kr.sjh.core.designsystem.components.EndlessLazyGridColumn
import kr.sjh.core.designsystem.components.LoadingComponent
import kr.sjh.core.designsystem.components.RefreshIndicator
import kr.sjh.core.designsystem.components.TextLine
import kr.sjh.core.designsystem.components.Title
import kr.sjh.core.designsystem.theme.DefaultAppBarHeight
import kr.sjh.core.model.adoption.Pet
import kr.sjh.feature.adoption.screen.filter.FilterCategoryList
import kr.sjh.feature.adoption.screen.filter.FilterViewModel
import kr.sjh.feature.adoption.state.AdoptionEvent
import kr.sjh.feature.adoption.state.AdoptionUiState
import kr.sjh.feature.adoption.state.FilterEvent
import kr.sjh.feature.adoption.state.FilterUiState
import kr.sjh.feature.adoption.state.SideEffect
import kotlin.math.roundToInt

@Composable
fun AdoptionRoute(
    viewModel: AdoptionViewModel = hiltViewModel(),
    filterViewModel: FilterViewModel = hiltViewModel(),
    navigateToPetDetail: (Pet) -> Unit
) {
    val adoptionUiState by viewModel.adoptionUiState.collectAsStateWithLifecycle()

    val filterUiState by filterViewModel.filterUiState.collectAsStateWithLifecycle()

    val Peek = SheetDetent("peek", calculateDetentHeight = { containerHeight, sheetHeight ->
        containerHeight
    })

    val bottomSheetState = rememberModalBottomSheetState(
        initialDetent = SheetDetent.Hidden, listOf(SheetDetent.Hidden, Peek)
    )

    val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = 0
    )

    LaunchedEffect(Unit) {
        filterViewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                SideEffect.HideBottomSheet -> {
                    bottomSheetState.currentDetent = SheetDetent.Hidden
                }

                SideEffect.ShowBottomSheet -> {
                    bottomSheetState.currentDetent = Peek
                }

                SideEffect.FetchPets -> {
                    viewModel.onEvent(
                        AdoptionEvent.Refresh(
                            filterUiState.toPetRequest()
                        )
                    )
                }
            }
        }
    }

    AdoptionScreen(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
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
    val scrollableHeight = DefaultAppBarHeight
    val appBarHeight = 114.dp
    val scrollableHeightPx = with(density) { scrollableHeight.roundToPx().toFloat() }
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
        PullToRefreshBox(state = state,
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
                        .padding(top = appBarHeight)
                        .size(50.dp), state = state, isRefreshing = adoptionUiState.isRefreshing
                )
            }) {
            EndlessLazyGridColumn(
                modifier = Modifier.fillMaxSize(),
                gridState = gridState,
                userScrollEnabled = !adoptionUiState.isRefreshing,
                items = adoptionUiState.pets,
                isLoadMore = adoptionUiState.isMore,
                contentPadding = PaddingValues(
                    top = appBarHeight + 10.dp, bottom = 10.dp, start = 5.dp, end = 5.dp
                ),
                itemKey = { item -> item.desertionNo },
                loadMore = {
                    if (!adoptionUiState.isMore) { // 중복 요청 방지
                        onEvent(
                            AdoptionEvent.LoadMore(
                                filterUiState.toPetRequest()
                            )
                        )
                    }
                },
            ) { item ->
                Pet(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(enabled = !adoptionUiState.isRefreshing) {
                            navigateToPetDetail(item)
                        }, pet = item
                )
            }
        }
        BeMyPetTopAppBar(modifier = Modifier
            .fillMaxWidth()
            .offset {
                IntOffset(
                    x = 0,
                    y = appbarOffsetHeightPx
                        .coerceIn(-scrollableHeightPx, 0f)
                        .roundToInt()
                )
            }
            .background(
                MaterialTheme.colorScheme.primary,
                RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)
            )
            .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)), title = {
            Title(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                title = stringResource(R.string.adoption),
                style = MaterialTheme.typography.headlineSmall
            )
        }, content = {
            FilterCategoryList(categories = filterUiState.categoryList,
                height = appBarHeight - scrollableHeight,
                onFilterEvent = { event ->
                    coroutineScope.launch {
                        gridState.scrollToItem(0)
                        appbarOffsetHeightPx = 0f
                    }
                    onFilterEvent(event)
                })
        })
        FilterComponent(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(MaterialTheme.colorScheme.background)
                .navigationBarsPadding(),
            filterUiState = filterUiState,
            sheetState = sheetState,
            onFilterEvent = onFilterEvent
        )
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
        Text(
            modifier = Modifier.padding(3.dp), text = "공고중", fontSize = 13.sp, color = Color.White
        )
    }
}