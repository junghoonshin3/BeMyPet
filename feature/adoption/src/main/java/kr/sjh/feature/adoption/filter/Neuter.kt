package kr.sjh.feature.adoption.filter

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kr.sjh.core.designsystem.components.CheckBoxButton
import kr.sjh.core.model.adoption.filter.Neuter
import kr.sjh.feature.adoption.state.AdoptionEvent
import kr.sjh.feature.adoption.state.AdoptionFilterOptionState
import kr.sjh.feature.adoption.state.AdoptionFilterState
import kr.sjh.feature.adoption.state.NeuterOptions

@Composable
fun Neuter(
    modifier: Modifier = Modifier,
    onEvent: (AdoptionEvent) -> Unit,
    optionState: AdoptionFilterOptionState
) {
    Column(modifier = modifier) {
        NeuterOptions.entries.forEach { option ->
            CheckBoxButton(
                title = option.title,
                selected = option.value == optionState.selectedNeuter.value
            ) {
                onEvent(
                    AdoptionEvent.SelectedNeuter(
                        neuter = Neuter(
                            value = option.value
                        )
                    )
                )
            }
        }
    }
}