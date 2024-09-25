package kr.sjh.feature.adoption.filter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kr.sjh.core.designsystem.components.CheckBoxButton
import kr.sjh.core.model.adoption.filter.UpKind
import kr.sjh.feature.adoption.state.AdoptionEvent
import kr.sjh.feature.adoption.state.AdoptionFilterState
import kr.sjh.feature.adoption.state.UpKindOptions

@Composable
fun UpKind(
    modifier: Modifier = Modifier,
    onEvent: (AdoptionEvent) -> Unit,
    adoptionFilterState: AdoptionFilterState
) {
    Column(modifier = modifier) {
        UpKindOptions.entries.forEach { upkind ->
            CheckBoxButton(
                title = upkind.title,
                selected = adoptionFilterState.selectedUpKind.upKindCd == upkind.cd
            ) {
                onEvent(
                    AdoptionEvent.SelectedUpKind(
                        upKind = UpKind(
                            upKindCd = upkind.cd,
                        )
                    )
                )
            }
        }
    }
}