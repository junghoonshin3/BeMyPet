package kr.sjh.feature.adoption.filter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kr.sjh.core.designsystem.components.CheckBoxButton
import kr.sjh.core.model.adoption.filter.State
import kr.sjh.feature.adoption.state.AdoptionEvent
import kr.sjh.feature.adoption.state.AdoptionFilterState
import kr.sjh.feature.adoption.state.StateOptions

@Composable
fun State(
    modifier: Modifier = Modifier,
    onEvent: (AdoptionEvent) -> Unit,
    adoptionFilterState: AdoptionFilterState
) {
    Column(modifier = modifier) {
        StateOptions.entries.forEach { option ->
            CheckBoxButton(
                title = option.title,
                selected = option.value == adoptionFilterState.selectedState.value
            ) {
                onEvent(
                    AdoptionEvent.SelectedState(
                        state = State(
                            value = option.value
                        )
                    )
                )
            }
        }
    }
}