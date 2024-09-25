package kr.sjh.feature.adoption.filter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kr.sjh.core.designsystem.components.DropDownMenu
import kr.sjh.feature.adoption.state.AdoptionEvent
import kr.sjh.feature.adoption.state.AdoptionFilterState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Area(
    modifier: Modifier = Modifier,
    onEvent: (AdoptionEvent) -> Unit,
    adoptionFilterState: AdoptionFilterState,
) {
    var expandedSido: Boolean by remember {
        mutableStateOf(false)
    }

    var expandedSigungu: Boolean by remember {
        mutableStateOf(false)
    }

    Row(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            DropDownMenu(list = adoptionFilterState.sidoList,
                expanded = expandedSido,
                selectedItem = adoptionFilterState.selectedSido,
                onExpandedChange = {
                    expandedSido = it
                },
                onDismissRequest = {
                    expandedSido = !expandedSido
                },
                selectedText = { item ->
                    TextField(
                        modifier = Modifier.menuAnchor(),
                        value = item.orgdownNm,
                        onValueChange = {},
                        readOnly = true
                    )
                },
                menuItem = { item ->
                    Box(modifier = Modifier
                        .height(30.dp)
                        .fillMaxWidth()
                        .clickable {
                            onEvent(
                                AdoptionEvent.SelectedSido(
                                    item
                                )
                            )
                            expandedSido = !expandedSido
                        }) {
                        Text(text = item.orgdownNm)
                    }
                })
        }
        Column(modifier = Modifier.weight(1f)) {
            DropDownMenu(list = adoptionFilterState.sigunguList,
                expanded = expandedSigungu,
                selectedItem = adoptionFilterState.selectedSigungu,
                onExpandedChange = {
                    expandedSigungu = it
                },
                onDismissRequest = {
                    expandedSigungu = !expandedSigungu
                },
                selectedText = { item ->
                    TextField(
                        modifier = Modifier.menuAnchor(),
                        value = item.orgdownNm,
                        onValueChange = {},
                        readOnly = true
                    )
                },
                menuItem = { item ->
                    //가정보호
                    if (item.orgCd == "6119999") return@DropDownMenu

                    Box(modifier = Modifier
                        .height(30.dp)
                        .fillMaxWidth()
                        .clickable {
                            onEvent(
                                AdoptionEvent.SelectedSigungu(
                                    item
                                )
                            )
                            expandedSigungu = !expandedSigungu
                        }) {
                        Text(text = item.orgdownNm)
                    }
                })
        }
    }
}