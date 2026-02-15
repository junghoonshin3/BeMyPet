package kr.sjh.feature.adoption.screen.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kr.sjh.core.designsystem.components.LoadingComponent
import kr.sjh.core.designsystem.components.RoundedCornerButton
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

    LaunchedEffect(sidoIndex) {
        sidoState.scrollToItem(sidoIndex) // 선택한 아이템으로 스크롤
    }

    LaunchedEffect(sigunguIndex) {
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
                .height(400.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(0.4f),
                state = sidoState,
                verticalArrangement = Arrangement.spacedBy(5.dp),
                contentPadding = PaddingValues(
                    top = 10.dp, bottom = 10.dp, start = 5.dp, end = 5.dp
                )
            ) {
                items(sidoList, key = { sido -> sido.orgCd }) { sido ->
                    RoundedCornerButton(modifier = Modifier.fillMaxSize(),
                        title = sido.orgdownNm,
                        selected = updateSido.orgCd == sido.orgCd,
                        onClick = {
                            if (updateSido.orgCd != sido.orgCd) {
                                updateSido = sido
                                onFilterEvent(FilterEvent.FetchSigungu(sido))
                            }
                        })
                }
            }
            Spacer(Modifier.width(5.dp))
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingComponent()
                    }
                    return@Box
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = sigunguState,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    contentPadding = PaddingValues(
                        top = 10.dp, bottom = 10.dp, start = 5.dp, end = 5.dp
                    )
                ) {
                    items(sigunguList, key = { sigungu -> sigungu.orgCd }) { sigungu ->
                        RoundedCornerButton(modifier = Modifier.fillMaxSize(),
                            title = sigungu.orgdownNm,
                            selected = updateSigungu.orgCd == sigungu.orgCd,
                            onClick = {
                                if (updateSigungu.orgCd != sigungu.orgCd) {
                                    updateSigungu = sigungu
                                }
                            })
                    }
                }
            }
        }
    }


}