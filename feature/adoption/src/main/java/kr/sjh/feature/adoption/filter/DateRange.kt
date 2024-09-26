package kr.sjh.feature.adoption.filter

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kr.sjh.feature.adoption.state.AdoptionEvent
import kr.sjh.feature.adoption.state.AdoptionFilterOptionState
import kr.sjh.feature.adoption.state.AdoptionFilterState

@Composable
fun DateRange(
    modifier: Modifier = Modifier,
    optionState: AdoptionFilterOptionState,
    onEvent: (AdoptionEvent) -> Unit
) {
    Row(modifier = modifier) {
        TextField(modifier = Modifier.weight(1f), placeholder = {
            Text("YYYYMMDD")
        }, value = optionState.selectedArea.start, onValueChange = {
            onEvent(
                AdoptionEvent.SelectedArea(
                    optionState.selectedArea.copy(
                        start = it
                    )
                )
            )
        })
        TextField(modifier = Modifier.weight(1f), placeholder = {
            Text("YYYYMMDD")
        }, value = optionState.selectedArea.end, onValueChange = {
            onEvent(
                AdoptionEvent.SelectedArea(
                    optionState.selectedArea.copy(
                        end = it
                    )
                )
            )
        })
    }
}