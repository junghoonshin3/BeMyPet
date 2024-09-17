package kr.sjh.feature.adoption.screen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.TopAppBar
import kr.sjh.core.designsystem.convertDpToPx
import kr.sjh.core.designsystem.modifier.centerPullToRefreshIndicator
import kr.sjh.core.model.adoption.Pet
import kr.sjh.feature.adoption.state.AdoptionEvent
import kr.sjh.feature.adoption.state.AdoptionUiState

@Composable
fun AdoptionRoute(viewModel: AdoptionViewModel = hiltViewModel()) {
    val adoptionUiState by viewModel.adoptionUiState.collectAsStateWithLifecycle()
    AdoptionScreen(adoptionUiState, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdoptionScreen(
    adoptionUiState: AdoptionUiState, onEvent: (AdoptionEvent) -> Unit
) {
    val pullToRefreshState = rememberPullToRefreshState()

    val context = LocalContext.current

    val threshold = PullToRefreshDefaults.PositionalThreshold

    val gridState = rememberLazyGridState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        PullToRefreshBox(modifier = Modifier.fillMaxSize(),
            isRefreshing = adoptionUiState.isLoading,
            state = pullToRefreshState,
            onRefresh = { onEvent(AdoptionEvent.Refresh) },
            indicator = {
                RefreshIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    state = pullToRefreshState,
                    isRefreshing = adoptionUiState.isLoading,
                    threshold = threshold
                )
            }) {
            adoptionUiState.pets?.let { pets ->
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxSize()
                        .offset {
                            IntOffset(
                                0, (pullToRefreshState.distanceFraction * threshold.convertDpToPx(
                                    context
                                )).toInt()
                            )
                        },
                    contentPadding = PaddingValues(5.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    userScrollEnabled = !adoptionUiState.isLoading
                ) {
                    items(pets, key = { it.desertionNo }) { pet ->
                        Pet(pet)
                    }
                }
            }

        }
    }

}


@Composable
private fun Pet(pet: Pet) {
    Box(modifier = Modifier
        .clip(RoundedCornerShape(10.dp))
        .size(100.dp, 200.dp)
        .clickable { }) {
        AsyncImage(
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            model = pet.popfile,
            contentDescription = "Pet"
        )
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

