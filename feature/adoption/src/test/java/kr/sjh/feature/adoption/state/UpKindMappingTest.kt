package kr.sjh.feature.adoption.state

import org.junit.Assert.assertEquals
import org.junit.Test

class UpKindMappingTest {

    @Test
    fun upKind_codes_are_mapped_correctly() {
        assertEquals("417000", UpKind.DOG.value)
        assertEquals("422400", UpKind.CAT.value)
        assertEquals("429900", UpKind.ETC.value)
    }

    @Test
    fun toPetRequest_serializes_selected_upkind_code() {
        assertEquals("417000", FilterUiState(selectedUpKind = UpKind.DOG).toPetRequest().upkind)
        assertEquals("422400", FilterUiState(selectedUpKind = UpKind.CAT).toPetRequest().upkind)
        assertEquals("429900", FilterUiState(selectedUpKind = UpKind.ETC).toPetRequest().upkind)
    }
}
