package kr.sjh.feature.adoption.screen.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kr.sjh.core.designsystem.components.LoadingComponent
import kr.sjh.core.designsystem.components.RoundedCornerButton
import kr.sjh.core.designsystem.theme.RoundedCorner18
import kr.sjh.core.model.adoption.filter.Sido
import kr.sjh.core.model.adoption.filter.Sigungu
import kr.sjh.feature.adoption.state.FilterEvent

@Composable
fun LocationContent(
    isLoading: Boolean,
    title: String,
    sidoList: List<Sido>,
    sigunguList: List<Sigungu>,
    selectedSido: Sido,
    selectedSigungu: Sigungu,
    onFilterEvent: (FilterEvent) -> Unit,
) {
    val sidoState = rememberLazyListState()

    val sigunguState = rememberLazyListState()

    var updateSido by remember(selectedSido) {
        mutableStateOf(selectedSido)
    }

    var updateSigungu by remember(selectedSigungu) {
        mutableStateOf(selectedSigungu)
    }

    // 선택한 인덱스를 찾기
    val sidoIndex = sidoList.indexOf(selectedSido).coerceAtLeast(0)
    val sigunguIndex = sigunguList.indexOf(selectedSigungu).coerceAtLeast(0)

    LaunchedEffect(Unit) {
        sidoState.scrollToItem(sidoIndex) // 선택한 아이템으로 스크롤
    }

    LaunchedEffect(Unit) {
        sigunguState.scrollToItem(sigunguIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        FilterTopBar(title = title, confirm = {
            onFilterEvent(FilterEvent.ConfirmLocation(updateSido, updateSigungu))
        }, close = {
            updateSido = selectedSido
            updateSigungu = selectedSigungu
            onFilterEvent(FilterEvent.CloseBottomSheet)
        })
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCorner18)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        RoundedCorner18
                    )
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    state = sidoState,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(
                        top = 8.dp, bottom = 8.dp, start = 8.dp, end = 8.dp
                    )
                ) {
                    items(sidoList, key = { sido -> sido.orgCd }) { sido ->
                        RoundedCornerButton(
                            modifier = Modifier.fillMaxWidth(),
                            title = sido.orgdownNm,
                            selected = updateSido.orgCd == sido.orgCd,
                            onClick = {
                                if (updateSido.orgCd != sido.orgCd) {
                                    updateSido = sido
                                    onFilterEvent(FilterEvent.FetchSigungu(sido))
                                }
                            }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCorner18)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        RoundedCorner18
                    )
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingComponent()
                    }
                    return@Box
                }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    state = sigunguState,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(
                        top = 8.dp, bottom = 8.dp, start = 8.dp, end = 8.dp
                    )
                ) {
                    items(sigunguList, key = { sigungu -> sigungu.orgCd }) { sigungu ->
                        RoundedCornerButton(
                            modifier = Modifier.fillMaxWidth(),
                            title = sigungu.orgdownNm,
                            selected = updateSigungu.orgCd == sigungu.orgCd,
                            onClick = {
                                if (updateSigungu.orgCd != sigungu.orgCd) {
                                    updateSigungu = sigungu
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
