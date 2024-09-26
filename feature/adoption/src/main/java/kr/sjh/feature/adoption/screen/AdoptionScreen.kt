package kr.sjh.feature.adoption.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieRetrySignal
import kotlinx.coroutines.launch
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.EndlessLazyGridColumn
import kr.sjh.core.designsystem.components.ExpandableLazyColumn
import kr.sjh.core.designsystem.components.FilterModalBottomSheet
import kr.sjh.core.designsystem.components.MultiSelectionFilterList
import kr.sjh.core.designsystem.components.SectionHeader
import kr.sjh.core.designsystem.convertDpToPx
import kr.sjh.core.designsystem.modifier.centerPullToRefreshIndicator
import kr.sjh.core.model.FilterBottomSheetState
import kr.sjh.core.model.adoption.Pet
import kr.sjh.feature.adoption.filter.Area
import kr.sjh.feature.adoption.filter.DateRange
import kr.sjh.feature.adoption.filter.Neuter
import kr.sjh.feature.adoption.filter.State
import kr.sjh.feature.adoption.filter.UpKind
import kr.sjh.feature.adoption.state.AdoptionEvent
import kr.sjh.feature.adoption.state.AdoptionFilterCategory
import kr.sjh.feature.adoption.state.AdoptionFilterOptionState
import kr.sjh.feature.adoption.state.AdoptionFilterState
import kr.sjh.feature.adoption.state.AdoptionUiState

@Composable
fun AdoptionRoute(viewModel: AdoptionViewModel = hiltViewModel()) {
    val adoptionUiState by viewModel.adoptionUiState.collectAsStateWithLifecycle()
    val adoptionFilterState by viewModel.adoptionFilterState.collectAsStateWithLifecycle()
    val selectedFilterOptions by viewModel.selectedFilterOptions.collectAsStateWithLifecycle()
    AdoptionScreen(
        adoptionUiState = adoptionUiState,
        adoptionFilterState = adoptionFilterState,
        selectedFilterOptions = selectedFilterOptions,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdoptionScreen(
    adoptionUiState: AdoptionUiState,
    adoptionFilterState: AdoptionFilterState,
    selectedFilterOptions: AdoptionFilterOptionState,
    onEvent: (AdoptionEvent) -> Unit
) {
    val pullToRefreshState = rememberPullToRefreshState()

    val context = LocalContext.current

    val threshold = PullToRefreshDefaults.PositionalThreshold

    val gridState = rememberLazyGridState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val coroutineScope = rememberCoroutineScope()

    val categories: List<AdoptionFilterCategory> = listOf(
        AdoptionFilterCategory.DateRange(
            categoryName = "기간"
        ), AdoptionFilterCategory.UpKind(
            categoryName = "축종"
        ), AdoptionFilterCategory.Area(
            categoryName = "지역"
        ), AdoptionFilterCategory.State(
            categoryName = "상태"
        ), AdoptionFilterCategory.Neuter(
            categoryName = "중성화 여부",
        )
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        MultiSelectionFilterList(modifier = Modifier.fillMaxWidth(),
            selectedItems = categories,
            onFilterType = { category ->
                if (!adoptionFilterState.selectedCategories.contains(category)) {
                    onEvent(
                        AdoptionEvent.FilterBottomSheetOpen(
                            bottomSheetState = FilterBottomSheetState.SHOW
                        )
                    )
                }
                onEvent(
                    AdoptionEvent.SelectedCategory(
                        category = category
                    )
                )

            },
            showFilter = {
                onEvent(
                    AdoptionEvent.FilterBottomSheetOpen(
                        bottomSheetState = FilterBottomSheetState.SHOW
                    )
                )
            })
        Text(
            modifier = Modifier
                .align(Alignment.End)
                .padding(end = 5.dp),
            text = "${adoptionUiState.pets.size}/${adoptionUiState.totalCount}건"
        )

        PullToRefreshBox(modifier = Modifier.fillMaxSize(),
            isRefreshing = adoptionUiState.isRefreshing,
            state = pullToRefreshState,
            onRefresh = { onEvent(AdoptionEvent.Refresh) },
            indicator = {
                RefreshIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    state = pullToRefreshState,
                    isRefreshing = adoptionUiState.isRefreshing,
                    threshold = threshold
                )
            }) {
            EndlessLazyGridColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .offset {
                        IntOffset(
                            0, (pullToRefreshState.distanceFraction * threshold.convertDpToPx(
                                context
                            )).toInt()
                        )
                    },
                gridState = gridState,
                userScrollEnabled = !adoptionUiState.isRefreshing,
                items = adoptionUiState.pets,
                itemKey = { it.desertionNo },
                loadMore = { onEvent(AdoptionEvent.LoadMore) },
            ) { item ->
                Pet(item)
            }
            FilterModalBottomSheet(
                containerColor = Color.White,
                onDismissRequest = {
                    onEvent(
                        AdoptionEvent.FilterBottomSheetOpen(
                            bottomSheetState = FilterBottomSheetState.HIDE
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 30.dp),
                sheetState = sheetState,
                bottomSheetType = adoptionFilterState.filterBottomSheetState,
            ) {
                ExpandableLazyColumn(modifier = Modifier.weight(1f),
                    headerItems = categories,
                    header = { category ->
                        SectionHeader(category = category, optionContent = { category ->
                            when (category) {
                                is AdoptionFilterCategory.DateRange -> {
                                    DateRange(
                                        modifier = Modifier.fillMaxWidth(),
                                        optionState = selectedFilterOptions,
                                        onEvent = onEvent,
                                    )
                                }

                                is AdoptionFilterCategory.Neuter -> {
                                    Neuter(
                                        modifier = Modifier.fillMaxWidth(),
                                        onEvent = onEvent,
                                        optionState = selectedFilterOptions,
                                    )
                                }

                                is AdoptionFilterCategory.State -> {
                                    State(
                                        modifier = Modifier.fillMaxWidth(),
                                        onEvent = onEvent,
                                        optionState = selectedFilterOptions,
                                    )
                                }

                                is AdoptionFilterCategory.UpKind -> {
                                    UpKind(
                                        modifier = Modifier.fillMaxWidth(),
                                        onEvent = onEvent,
                                        optionState = selectedFilterOptions,
                                    )
                                }

                                is AdoptionFilterCategory.Area -> {
                                    Area(
                                        modifier = Modifier.fillMaxWidth(),
                                        onEvent = onEvent,
                                        adoptionFilterState = adoptionFilterState,
                                        optionState = selectedFilterOptions,
                                    )
                                }
                            }
                        })
                    })
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = {
                        onEvent(
                            AdoptionEvent.SelectedInit
                        )
                    }) {
                        Text("선택 초기화")
                    }
                    Button(onClick = {
                        coroutineScope.launch {
                            gridState.animateScrollToItem(0)
                        }
                        onEvent(
                            AdoptionEvent.Apply
                        )
                    }) {
                        Text("적용하기")
                    }
                }
            }
        }
    }
}


@Composable
private fun Pet(pet: Pet) {
    val context = LocalContext.current
    val imageRequest = ImageRequest.Builder(context).data(pet.popfile).build()
    Box(modifier = Modifier
        .clip(RoundedCornerShape(10.dp))
        .size(100.dp, 200.dp)
        .clickable { }) {
        SubcomposeAsyncImage(contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            model = imageRequest,
            contentDescription = "Pet",
            loading = {
                LottieLoading()
            })
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RefreshIndicator(
    modifier: Modifier = Modifier, state: PullToRefreshState, isRefreshing: Boolean, threshold: Dp
) {
    val context = LocalContext.current

    val imageVector by remember {
        derivedStateOf {
            when {
                state.distanceFraction > 0f && state.distanceFraction <= 0.5f && !isRefreshing -> {
                    ImageVector.vectorResource(
                        res = context.resources, resId = R.drawable.dog_face_svgrepo_com
                    )
                }

                state.distanceFraction <= 1f && state.distanceFraction > 0.5f && !isRefreshing -> {
                    ImageVector.vectorResource(
                        res = context.resources, resId = R.drawable.cat_svgrepo_com
                    )
                }

                state.distanceFraction > 1f && !isRefreshing -> {
                    ImageVector.vectorResource(
                        res = context.resources, resId = R.drawable.panda_face_1_svgrepo_com
                    )
                }

                else -> {
                    ImageVector.vectorResource(
                        res = context.resources, resId = R.drawable.sheep_2_svgrepo_com
                    )
                }
            }
        }
    }
    Box(
        modifier = modifier.centerPullToRefreshIndicator(
            state = state, shape = RectangleShape, threshold = threshold
        ), contentAlignment = Alignment.TopCenter
    ) {
        if (state.distanceFraction == 0f) {
            return@Box
        }

        if (isRefreshing) {
            Text(text = context.resources.getString(R.string.refreshing), fontSize = 25.sp)
        } else {
            Image(imageVector = imageVector, contentDescription = "animal")
        }
    }
}

@Composable
private fun LottieLoading() {
    val retrySignal = rememberLottieRetrySignal()
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.loading),
        onRetry = { failCount, exception ->
            retrySignal.awaitRetry()
            true
        })
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
    )
    LottieAnimation(
        composition = composition,
        progress = { progress },
    )
}
