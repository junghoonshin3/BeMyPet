package kr.sjh.feature.adoption.screen

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import com.composables.core.ModalBottomSheet
import com.composables.core.Scrim
import com.composables.core.Sheet
import com.composables.core.SheetDetent
import com.composables.core.SheetDetent.Companion.Hidden
import kotlinx.coroutines.launch
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.BeMyPetTopAppBar
import kr.sjh.core.designsystem.components.EndlessLazyGridColumn
import kr.sjh.core.designsystem.components.LoadingComponent
import kr.sjh.core.designsystem.components.RefreshIndicator
import kr.sjh.core.designsystem.components.RoundedCornerButton
import kr.sjh.core.designsystem.components.TextLine
import kr.sjh.core.designsystem.theme.DefaultAppBarHeight
import kr.sjh.core.designsystem.theme.BeMyPetTheme
import kr.sjh.core.model.adoption.Pet
import kr.sjh.feature.adoption.screen.filter.CategoryType
import kr.sjh.feature.adoption.screen.filter.DateRangePickerModal
import kr.sjh.feature.adoption.screen.filter.FilterContent
import kr.sjh.feature.adoption.state.AdoptionEvent
import kr.sjh.feature.adoption.state.AdoptionFilterState
import kr.sjh.feature.adoption.state.AdoptionUiState
import kotlin.math.roundToInt

@Composable
fun AdoptionRoute(
    viewModel: AdoptionViewModel = hiltViewModel(), navigateToPetDetail: (Pet) -> Unit
) {
    val adoptionUiState by viewModel.adoptionUiState.collectAsStateWithLifecycle()

    val filterState by viewModel.adoptionFilterState.collectAsStateWithLifecycle()

    val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = 0
    )

    AdoptionScreen(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        adoptionUiState = adoptionUiState,
        adoptionFilterState = filterState,
        gridState = gridState,
        navigateToPetDetail = navigateToPetDetail,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdoptionScreen(
    modifier: Modifier = Modifier,
    adoptionUiState: AdoptionUiState,
    adoptionFilterState: AdoptionFilterState,
    gridState: LazyGridState,
    navigateToPetDetail: (Pet) -> Unit,
    onEvent: (AdoptionEvent) -> Unit
) {

    val density = LocalDensity.current

    var isDatePickerShow by remember { mutableStateOf(false) }

    val peek = SheetDetent(identifier = "peek") { containerHeight, sheetHeight ->
        containerHeight * 0.6f
    }

    val sheetState = com.composables.core.rememberModalBottomSheetState(
        initialDetent = Hidden, detents = listOf(Hidden, peek)
    )
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

        BeMyPetTopAppBar(modifier = Modifier
            .fillMaxWidth()
            .zIndex(1f)
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = stringResource(R.string.adoption),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }, content = {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(appBarHeight - scrollableHeight)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    IconButton(
                        modifier = Modifier.padding(start = 5.dp, end = 5.dp),
                        onClick = {
                            onEvent(AdoptionEvent.InitCategory)
                            coroutineScope.launch {
                                gridState.animateScrollToItem(0, 0)
                                appbarOffsetHeightPx = 0f
                            }
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
                                    sheetState.currentDetent = peek
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
        })

        PullToRefreshBox(state = state,
            modifier = Modifier.fillMaxSize(),
            isRefreshing = adoptionUiState.isRefreshing,
            onRefresh = {
                onEvent(AdoptionEvent.Refresh)
                appbarOffsetHeightPx = 0f
            },
            indicator = {
                RefreshIndicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = appBarHeight),
                    state = state,
                    isRefreshing = adoptionUiState.isRefreshing
                )
            }) {
            if (adoptionUiState.totalCount == 0) {
                val scrollState = rememberScrollState()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "펫이 없어요!",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            } else {
                EndlessLazyGridColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    gridState = gridState,
                    userScrollEnabled = !adoptionUiState.isRefreshing,
                    items = adoptionUiState.pets,
                    contentPadding = PaddingValues(
                        top = appBarHeight + 10.dp, bottom = 10.dp, start = 5.dp, end = 5.dp
                    ),
                    itemKey = { item -> item.desertionNo },
                    loadMore = {
                        Log.d("sjh", "loadmore")
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
                                navigateToPetDetail(item)
                            }, pet = item
                    )
                }
            }
        }
        ModalBottomSheet(state = sheetState) {
            Scrim()
            Sheet(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .navigationBarsPadding(),
                enabled = false
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

@Preview(
    showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "DefaultPreviewDark"
)
@Composable
fun AdoptionScreenPreview() {
    BeMyPetTheme() {
        AdoptionScreen(modifier = Modifier
            .fillMaxSize()
            .padding(5.dp),
            adoptionUiState = AdoptionUiState(pets = (0..100).map { Pet(desertionNo = it.toString()) }),
            adoptionFilterState = AdoptionFilterState(),
            gridState = rememberLazyGridState(),
            navigateToPetDetail = {},
            onEvent = {})
    }
}