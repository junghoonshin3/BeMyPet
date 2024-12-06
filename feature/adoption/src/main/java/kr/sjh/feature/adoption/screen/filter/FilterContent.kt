package kr.sjh.feature.adoption.screen.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.core.ModalBottomSheetState
import com.composables.core.SheetDetent
import kr.sjh.core.designsystem.components.CheckBoxButton
import kr.sjh.core.designsystem.components.RoundedCornerButton
import kr.sjh.core.model.adoption.filter.Sido
import kr.sjh.core.model.adoption.filter.Sigungu
import kr.sjh.feature.adoption.state.AdoptionEvent
import kr.sjh.feature.adoption.state.AdoptionFilterState
import kr.sjh.feature.adoption.state.Neuter
import kr.sjh.feature.adoption.state.UpKind

@Composable
fun FilterContent(
    adoptionFilterState: AdoptionFilterState,
    sheetState: ModalBottomSheetState,
    onEvent: (AdoptionEvent) -> Unit,
) {
    adoptionFilterState.selectedCategory?.let { category ->
        when (category.type) {
            CategoryType.DATE_RANGE -> {}

            CategoryType.NEUTER -> {
                NeuterContent(title = category.type.title,
                    selectedNeuter = adoptionFilterState.selectedNeuter,
                    confirm = { neuter ->
                        onEvent(
                            AdoptionEvent.SelectedNeuter(neuter)
                        )
                        category.isSelected.value = neuter.value != null
                        category.displayNm.value =
                            if (category.isSelected.value) neuter.title else category.type.title
                        sheetState.currentDetent = SheetDetent.Hidden
                    },
                    close = {
                        sheetState.currentDetent = SheetDetent.Hidden
                    })
            }

            CategoryType.LOCATION -> {
                if (adoptionFilterState.isLocationError) {
                    LocationError(onEvent)
                    return
                }
                LocationContent(title = category.type.title,
                    sidoList = adoptionFilterState.sidoList,
                    sigunguList = adoptionFilterState.sigunguList,
                    selectedSido = adoptionFilterState.selectedSido,
                    selectedSigungu = adoptionFilterState.selectedSigungu,
                    confirm = { sido, sigungu ->
                        onEvent(
                            AdoptionEvent.SelectedLocation(sido, sigungu)
                        )
                        category.isSelected.value = sido.orgCd != null
                        category.displayNm.value =
                            if (sido.orgCd == null) category.type.title else "${sido.orgdownNm} ${sigungu.orgdownNm}"
                        sheetState.currentDetent = SheetDetent.Hidden
                    },
                    close = {
                        sheetState.currentDetent = SheetDetent.Hidden
                    },
                    onSido = {
                        onEvent(AdoptionEvent.LoadSigungu(it))
                    })
            }

            CategoryType.UP_KIND -> {
                UpKindContent(title = category.type.title, close = {
                    sheetState.currentDetent = SheetDetent.Hidden
                }, selectedUpKind = adoptionFilterState.selectedUpKind, confirm = { upKind ->
                    onEvent(
                        AdoptionEvent.SelectedUpKind(
                            upKind
                        )
                    )
                    category.isSelected.value = upKind.value != null
                    category.displayNm.value =
                        if (category.isSelected.value) upKind.title else category.type.title
                    sheetState.currentDetent = SheetDetent.Hidden
                })
            }
        }
    }
}

@Composable
private fun NeuterContent(
    title: String, selectedNeuter: Neuter, confirm: (Neuter) -> Unit, close: () -> Unit
) {
    var updateNeuter by remember {
        mutableStateOf(selectedNeuter)
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        FilterTopBar(title = title, close = close, confirm = {
            confirm(updateNeuter)
        })
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        ) {
            items(Neuter.entries.toTypedArray()) { item ->
                CheckBoxButton(modifier = Modifier.fillMaxWidth(),
                    title = item.title,
                    selected = updateNeuter == item,
                    onClick = {
                        updateNeuter = item
                    })
            }
        }
    }

}

@Composable
private fun LocationContent(
    title: String,
    sidoList: List<Sido>,
    sigunguList: List<Sigungu>,
    selectedSido: Sido,
    selectedSigungu: Sigungu,
    confirm: (Sido, Sigungu) -> Unit = { _, _ -> },
    onSido: (Sido) -> Unit = { _ -> },
    close: () -> Unit = {}
) {
    var updateSido by remember {
        mutableStateOf(selectedSido)
    }
    var updateSigungu by remember {
        mutableStateOf(selectedSigungu)
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        FilterTopBar(title = title, confirm = {
            confirm(updateSido, updateSigungu)
        }, close = close)
        Row(modifier = Modifier.fillMaxWidth()) {
            LazyColumn(
                modifier = Modifier.weight(0.4f), verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                items(sidoList) { sido ->
                    RoundedCornerButton(modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp),
                        title = sido.orgdownNm,
                        selected = updateSido.orgCd == sido.orgCd,
                        onClick = {
                            if (updateSido.orgCd != sido.orgCd) {
                                onSido(sido)
                                updateSido = sido
                                updateSigungu = Sigungu()
                            }
                        })
                }
            }
            Spacer(Modifier.width(5.dp))
            LazyColumn(
                modifier = Modifier.weight(0.6f), verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                items(sigunguList) { sigungu ->
                    RoundedCornerButton(modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp),
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

@Composable
private fun LocationError(onEvent: (AdoptionEvent) -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        RoundedCornerButton(modifier = Modifier.padding(10.dp), textStyle = TextStyle(
            fontSize = 26.sp
        ), title = "재시도", onClick = {
            onEvent(
                AdoptionEvent.LoadSido
            )
        })
    }
}

@Composable
private fun UpKindContent(
    title: String,
    selectedUpKind: UpKind,
    confirm: (UpKind) -> Unit = {},
    close: () -> Unit = {},
) {
    var updateUpKind by remember {
        mutableStateOf(selectedUpKind)
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        FilterTopBar(title = title, close = close, confirm = {
            confirm(updateUpKind)
        })
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        ) {
            items(UpKind.entries.toTypedArray()) { item ->
                CheckBoxButton(modifier = Modifier.fillMaxWidth(),
                    title = item.title,
                    selected = updateUpKind == item,
                    onClick = {
                        updateUpKind = item
                    })
            }
        }
    }
}