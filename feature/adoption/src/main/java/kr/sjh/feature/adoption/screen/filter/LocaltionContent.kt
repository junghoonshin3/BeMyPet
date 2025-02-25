package kr.sjh.feature.adoption.screen.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
//    confirm: (Sido, Sigungu) -> Unit = { _, _ -> },
//    onSido: (Sido) -> Unit = { _ -> },
//    close: () -> Unit = {}
) {
    var updateSido by remember {
        mutableStateOf(selectedSido)
    }
    var updateSigungu by remember {
        mutableStateOf(selectedSigungu)
    }
    Column(modifier = Modifier.fillMaxSize()) {
        FilterTopBar(title = title, confirm = {
//            confirm(updateSido, updateSigungu)
        }, close = {
            onFilterEvent(FilterEvent.CloseBottomSheet)
        })
        Row(modifier = Modifier.fillMaxWidth()) {
            LazyColumn(
                modifier = Modifier.weight(0.4f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                contentPadding = PaddingValues(
                    top = 10.dp, bottom = 10.dp, start = 5.dp, end = 5.dp
                )
            ) {
                items(sidoList) { sido ->
                    RoundedCornerButton(modifier = Modifier.fillMaxSize(),
                        title = sido.orgdownNm,
                        selected = updateSido.orgCd == sido.orgCd,
                        onClick = {
                            if (updateSido.orgCd != sido.orgCd) {
                                onFilterEvent(FilterEvent.SelectedSido(sido))
                                updateSido = sido
                                updateSigungu = Sigungu()
                            }
                        })
                }
            }
            Spacer(Modifier.width(5.dp))
            Box(modifier = Modifier.weight(0.6f)) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingComponent()
                    }
                    return@Box
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    contentPadding = PaddingValues(
                        top = 10.dp, bottom = 10.dp, start = 5.dp, end = 5.dp
                    )
                ) {
                    items(sigunguList) { sigungu ->
                        RoundedCornerButton(modifier = Modifier.fillMaxSize(),
                            title = sigungu.orgdownNm,
                            selected = updateSigungu.orgCd == sigungu.orgCd,
                            onClick = {
                                updateSigungu = sigungu
                            })
                    }

                }
            }
        }
    }


}